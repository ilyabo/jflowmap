package jflowmap.data;

import java.io.IOException;
import java.util.List;

import jflowmap.util.IOUtils;

import org.apache.log4j.Logger;
import org.geotools.dbffile.Dbf;
import org.geotools.dbffile.DbfFileException;
import org.geotools.shapefile.Shapefile;

import prefuse.data.Table;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.collections.IntIterator;
import at.fhj.utils.misc.FileUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Ilya Boyandin
 */
public class ShapefileReader {

  private static Logger logger = Logger.getLogger(ShapefileReader.class);

  private ShapefileReader() {
  }

  public static Iterable<Geometry> loadShapefile(String location,
      String dbfAreaIdField,
      String dbfSelectShapesWhere)
      throws IOException {

    logger.info("Loading shapefile '" + location + "'");

    List<Geometry> geoms = asList(loadShapefile(location));

    if (dbfAreaIdField != null  ||   dbfSelectShapesWhere != null) {
      String fname = FileUtils.getFilename(location);
      String fpath = location.substring(0, location.length() - fname.length());
      String dbfLocation = fpath + fname.replace(".shp", ".dbf");

      Dbf dbf = loadDbf(dbfLocation);
      Table table = asTable(dbf);  // create table which we can query and filter

      if (dbfAreaIdField != null) {
        if (table.getColumn(dbfAreaIdField) == null) {
          throw new IllegalArgumentException("Cannot read DBF column '" + dbfAreaIdField + "'");
        }

        for (int i = 0; i < geoms.size(); i++) {
          Geometry g = geoms.get(i);
          g.setUserData(table.get(i, dbfAreaIdField));
        }
      }


      if (dbfSelectShapesWhere != null) {
        List<Geometry> filtered = Lists.newArrayList();
        IntIterator rows = table.rows((Predicate) ExpressionParser.parse(dbfSelectShapesWhere, true));
        while (rows.hasNext()) {
          filtered.add(geoms.get(rows.nextInt()));
        }
        geoms = filtered;
      }

    }

    return geoms;
  }

  private static List<Geometry> asList(GeometryCollection gc) {
    List<Geometry> geoms = Lists.newArrayList();
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      geoms.add(gc.getGeometryN(i));
    }
    return geoms;
  }

  public static Table asTable(Dbf dbf) throws IOException {
    Table table = new Table();
    for (int i = 0; i < dbf.getNumFields(); i++) {
      Class<?> type = dbfTypeToClass(dbf.getFieldType(i));
      table.addColumn(dbf.getFieldName(i).toString(), type);
    }

    table.addRows(dbf.getLastRec());

    for (int i = 0; i < dbf.getNumFields(); i++) {
      List<?> values = getColumnValues(dbf, i);
      for (int row = 0; row < values.size(); row++) {
        table.set(row, i, values.get(row));
      }
    }
    return table;
  }


  private static Class<?> dbfTypeToClass(char dbfFieldType) {
    // See http://www.dbase.com/KnowledgeBase/int/db7_file_fmt.htm
    switch (dbfFieldType) {
    case 'N':
    case 'F':
    case 'O':
      return Double.class;
    case 'I':
      return Integer.class;
    default:
      return String.class;
    }
  }

  private static List<?> getColumnValues(Dbf dbf, int col) throws IOException {
    try {
      switch (dbf.getFieldType(col)) {
      case 'N':
      case 'F':
      case 'O':
        List<Double> list = Lists.newArrayList();
        for (float f : dbf.getFloatCol(col)) { list.add((double)f); }
        return list;
      case 'I':
        return ImmutableList.copyOf(dbf.getIntegerCol(col));

      default:
        return ImmutableList.copyOf(dbf.getStringCol(col));
      }
    } catch (DbfFileException dfe) {
      throw new IOException(dfe);
    }
  }

  private static GeometryCollection loadShapefile(String shpLocation) throws IOException {
    try {
      Shapefile shapefile = new Shapefile(IOUtils.asInputStream(shpLocation));
      return shapefile.read(new GeometryFactory());
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public static Dbf loadDbf(String dbfLocation) throws IOException {
    try {
      logger.info("Attempting to load .dbf for the shapefile from '" + dbfLocation + "'");
      Dbf dbf = new Dbf(IOUtils.asInputStream(dbfLocation));
      return dbf;
    } catch (DbfFileException dfe) {
      throw new IOException(dfe);
    }
  }

  private static String[] getDbfStringColumnValues(Dbf dbf, int col) throws IOException {
    try {
      return dbf.getStringCol(col);
    } catch (DbfFileException dfe) {
      throw new IOException(dfe);
    }
  }

  private static String[] getDbfIdColumnValues(Dbf dbf, String dbfAreaIdField) throws IOException {
    int col = findDbfColumnByName(dbf, dbfAreaIdField);
    logger.info("dbfAreaIdField: '" + dbfAreaIdField + "'. Available fields: '" + listDbfFields(dbf) + "'");
    if (col < 0) {
      throw new IOException("Field '" + dbfAreaIdField + "' not found in dbf file. " +
          "Available fields: '" + listDbfFields(dbf) + "'");
    }
    return getDbfStringColumnValues(dbf, col);
  }

  private static String listDbfFields(Dbf dbf) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < dbf.getNumFields(); i++) {
      sb.append(dbf.getFieldName(i)).append(",");
    }
    return sb.toString();
  }

  public static int findDbfColumnByName(Dbf dbf, String colName) {
    for (int i = 0; i < dbf.getNumFields(); i++) {
      if (colName.equals(dbf.getFieldName(i).toString())) {
        return i;
      }
    }
    return -1;
  }

}
