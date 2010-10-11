package jflowmap.util;

import java.util.ArrayList;
import java.util.List;

import prefuse.data.Table;

/**
 * @author Ilya Boyandin
 */
public class Tables {

  private Tables() {
  }

  public static Iterable<String> columns(Table table) {
    List<String> list = new ArrayList<String>(table.getColumnCount());
    for (int i = 0; i < table.getColumnCount(); i++) {
      list.add(table.getColumnName(i));
    }
    return list;
  }

}
