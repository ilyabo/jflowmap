/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Boyandin
 */
public enum BagOfWordsFilter {

  ALL {
    @Override
    protected boolean matches(String[] words, String[] queryWords) {
      for (String qw : queryWords) {
        if (qw.length() > 0   &&  !containsWordStartingWith(words, qw)) return false;
      }
      return true;
    }
  },

  ANY {
    @Override
    protected boolean matches(String[] words, String[] queryWords) {
      int checked = 0;
      for (String qw : queryWords) {
        if (qw.length() > 0) {  // in some cases split puts an empty string in the array
          checked++;
          if (containsWordStartingWith(words, qw)) return true;
        }
      }
      return (checked == 0);
    }
  };

  public boolean apply(String str, String query) {
    return apply(str, words(query));
  }

  public boolean apply(String str, String[] queryWords) {
    if (queryWords.length == 0  ||
       (queryWords.length == 1  &&  queryWords[0].length() == 0)) {
      return true;
    }
    return matches(words(str), queryWords);
  }

  private static boolean containsWordStartingWith(String[] words, String word) {
    for (String w : words) {
      if (w.startsWith(word)) return true;
    }
    return false;
  }

  protected abstract boolean matches(String[] words, String[] queryWords);

  public static String[] words(String str) {
    return str.trim().toLowerCase().split("[^\\p{Alnum}]+");
  }

  /**
   * Splits the string into groups separated by commas, and returns words
   * of each of the groups.
   */
  public static List<String[]> wordGroups(String str) {
    String[] groups = str.split(",");
    List<String[]> list = new ArrayList<String[]>(groups.length);
    for (String g : groups) {
      g = g.trim();
      if (g.length() > 0) {
        String[] words = words(g);
        if (words.length > 0) {
          list.add(words);
        }
      }
    }
    return list;
  }

}
