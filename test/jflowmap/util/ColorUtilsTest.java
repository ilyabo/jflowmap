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

package jflowmap.util;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;


/**
 * @author Ilya Boyandin
 */
public class ColorUtilsTest {


  @Test
  public void testToHexString() {
    assertEquals("00f0a2", ColorUtils.toHexString(new Color(0x00f0a2)));
    assertEquals("abf000", ColorUtils.toHexString(new Color(0xabf000)));
    assertEquals("ffffff", ColorUtils.toHexString(new Color(0xffffff)));
    assertEquals("ffffff", ColorUtils.toHexString(new Color(0xffffffff)));
    assertEquals("ffffff", ColorUtils.toHexString(Color.white));
    assertEquals("00ff00", ColorUtils.toHexString(Color.green));
  }

}
