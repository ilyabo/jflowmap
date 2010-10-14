package jflowmap.data;

import java.io.IOException;

import jflowmap.util.IOUtils;

import org.geotools.dbffile.Dbf;
import org.geotools.dbffile.DbfFileException;
import org.geotools.shapefile.Shapefile;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Ilya Boyandin
 */
public class ShapefileReader {

  private ShapefileReader() {
  }

  public static GeometryCollection loadShapefile(String shpLocation, String dbfLocation, String dbfIdColumn)
      throws IOException {

    GeometryCollection geomColl = loadShapefile(shpLocation);
    if (dbfLocation != null  &&  dbfIdColumn != null) {
      String[] values = getDbfIdColumnValues(dbfLocation, dbfIdColumn);

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

  private static String[] getDbfIdColumnValues(String dbfLocation, String dbfIdColumn) throws IOException {
    try {
      Dbf dbf = new Dbf(IOUtils.asInputStream(dbfLocation));
      int col = findDbfColumnByName(dbf, dbfIdColumn);
      if (col < 0) {
        throw new IOException("Column '" + dbfIdColumn + "' not found in file: " + dbfLocation);
      }
      return getDbfStringColumnValues(dbf, col);
    } catch (DbfFileException dfe) {
      throw new IOException(dfe);
    }
  }

  private static String[] getDbfStringColumnValues(Dbf dbf, int col) throws DbfFileException, IOException {
    return dbf.getStringCol(col);
  }

  public static int findDbfColumnByName(Dbf dbf, String colName) {
    for (int i = 0; i < dbf.getNumFields(); i++) {
      if (dbf.getFieldName(i).equals(colName)) {
        return i;
      }
    }
    return -1;
  }

}
