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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Ilya Boyandin
 */
public class BagOfWordsFilterTest {

  /**
   * Test method for {@link jflowmap.util.BagOfWordsFilter#apply(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testApplyStringString() {
    assertTrue(BagOfWordsFilter.ALL.apply("This is a  Unit\n testing!", "test,unit,is"));
    assertTrue(!BagOfWordsFilter.ALL.apply("This is a Unit test!", "testing unit is"));
    assertTrue(!BagOfWordsFilter.ALL.apply("testing", "detesting"));
    assertTrue(!BagOfWordsFilter.ALL.apply("This is a Unit test!", "test unit is not"));
    assertTrue(BagOfWordsFilter.ALL.apply("This is not a LOVE song, but a Unit test!",
        "test unit is not love song"));
    assertTrue(BagOfWordsFilter.ALL.apply("+7-(812)-212-85-06", "7 812 212 85 06"));
    assertTrue(!BagOfWordsFilter.ALL.apply("3.3", "33"));
    assertTrue(BagOfWordsFilter.ALL.apply("Mühe", "MÜHE"));
    assertTrue(BagOfWordsFilter.ALL.apply("EMPTY QUERY", ""));
    assertTrue(!BagOfWordsFilter.ALL.apply("", "EMPTY STRING"));
    assertTrue(BagOfWordsFilter.ALL.apply("", ""));
    assertTrue(BagOfWordsFilter.ALL.apply("Repetition is the best training.", "repet repet best best"));


    assertTrue(BagOfWordsFilter.ANY.apply("This is a  Unit\n testing!", "test,unit,is"));
    assertTrue(!BagOfWordsFilter.ANY.apply("This is a Unit test!", "testing"));
    assertTrue(!BagOfWordsFilter.ANY.apply("testing", "detesting"));
    assertTrue(BagOfWordsFilter.ANY.apply("This is a Unit test!", "test unit is not"));
    assertTrue(BagOfWordsFilter.ANY.apply("This is not a LOVE song, but a Unit test!",
        "test unit is not love song"));
    assertTrue(BagOfWordsFilter.ANY.apply("+7-(812)-212-85-06", "7 812 212 85 06"));
    assertTrue(!BagOfWordsFilter.ANY.apply("3.3", "33"));
    assertTrue(BagOfWordsFilter.ANY.apply("Mühe", "MÜHE"));
    assertTrue(BagOfWordsFilter.ANY.apply("EMPTY QUERY", ""));
    assertTrue(!BagOfWordsFilter.ANY.apply("", "EMPTY STRING"));
    assertTrue(BagOfWordsFilter.ANY.apply("", ""));
    assertTrue(BagOfWordsFilter.ALL.apply("Repetition is the best training.", "repet repet best best"));
  }

}
