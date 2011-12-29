package jflowmap.util;

import java.io.IOException;
import java.io.InputStream;

import prefuse.util.io.IOLib;

/**
 * @author Ilya Boyandin
 */
public class IOUtils {

  private IOUtils() {
  }

  public static InputStream asInputStream(String location) throws IOException {
    InputStream is = IOLib.streamFromString(location);
    if (is == null) {
      throw new IOException("Cannot read from location: " + location);
    } else {
      return is;
    }
  }


}
