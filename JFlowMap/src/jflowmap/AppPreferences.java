/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap;

import java.util.prefs.Preferences;

/**
 * @author Ilya Boyandin
 */
public enum AppPreferences {

  INSTANCE;

  enum Names {
    FILE_OPEN_LAST_VISITED_DIR;
  }

  private static Preferences prefs = Preferences.userNodeForPackage(JFlowMapMain.class);

  public String getFileOpenLastVisitedDir() {
    return prefs.get(Names.FILE_OPEN_LAST_VISITED_DIR.name(), "");
  }

  public void setFileOpenLastVisitedDir(String dir) {
    prefs.put(Names.FILE_OPEN_LAST_VISITED_DIR.name(), dir);
  }

}
