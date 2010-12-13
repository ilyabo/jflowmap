package jflowmap.data;

import java.io.IOException;

import jflowmap.util.IOUtils;

import org.apache.log4j.Logger;
import org.geotools.dbffile.Dbf;
import org.geotools.dbffile.DbfFileException;
import org.geotools.shapefile.Shapefile;

import at.fhj.utils.misc.FileUtils;

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

  public static GeometryCollection loadShapefile(String location, String dbfAreaIdField)
      throws IOException {

    logger.info("Loading shapefile '" + location + "'");

    GeometryCollection geomColl = loadShapefile(location);

    if (dbfAreaIdField != null) {
      String fname = FileUtils.getFilename(location);
      String fpath = location.substring(0, location.length() - fname.length());
      String dbfLocation = fpath + fname.replace(".shp", ".dbf");

      String[] values = getDbfIdColumnValues(dbfLocation, dbfAreaIdField);

      for (int i = 0; i < geomColl.getNumGeometries(); i++) {
        Geometry g = geomColl.getGeometryN(i);
        g.setUserData(values[i]);
      }
    }

    return geomColl;
  }

  private static GeometryCollection loadShapefile(String shpLocation) throws IOException {
    try {
      Shapefile shapefile = new Shapefile(IOUtils.asInputStream(shpLocation));
      return shapefile.read(new GeometryFactory());
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private static String[] getDbfIdColumnValues(String dbfLocation, String dbfAreaIdField) throws IOException {
    try {
      logger.info("Attempting to load .dbf for the shapefile from '" + dbfLocation + "'");
      Dbf dbf = new Dbf(IOUtils.asInputStream(dbfLocation));
      int col = findDbfColumnByName(dbf, dbfAreaIdField);
      logger.info("dbfAreaIdField: '" + dbfAreaIdField + "'. Available fields: '" + listDbfFields(dbf) + "'");
      if (col < 0) {
        throw new IOException("Field '" + dbfAreaIdField + "' not found in dbf file: '" + dbfLocation +
            "'. Available fields: '" + listDbfFields(dbf) + "'");
      }
      return getDbfStringColumnValues(dbf, col);
    } catch (DbfFileException dfe) {
      throw new IOException(dfe);
    }
  }

  private static String listDbfFields(Dbf dbf) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < dbf.getNumFields(); i++) {
      sb.append(dbf.getFieldName(i)).append(",");
    }
    return sb.toString();
  }

  private static String[] getDbfStringColumnValues(Dbf dbf, int col) throws DbfFileException, IOException {
    return dbf.getStringCol(col);
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
