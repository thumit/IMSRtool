/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Operations on {@link java.lang.String} that are
 * <code>null</code> safe.

 *
 * @see java.lang.String
 * @author <a href="http://jakarta.apache.org/turbine/">Apache Jakarta Turbine</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:gcoladonato@yahoo.com">Greg Coladonato</a>
 * @author <a href="mailto:ed@apache.org">Ed Korthof</a>
 * @author <a href="mailto:rand_mcneely@yahoo.com">Rand McNeely</a>
 * @author Stephen Colebourne
 * @author <a href="mailto:fredrik@westermarck.com">Fredrik Westermarck</a>
 * @author Holger Krauth
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author Arun Mammen Thomas
 * @author Gary Gregory
 * @author Phil Steitz
 * @author Al Chou
 * @author Michael Davey
 * @author Reuben Sivan
 * @author Chris Hyzer
 * @author Scott Johnson
 * @since 1.0
 * @version $Id: StringUtils.java 635447 2008-03-10 06:27:09Z bayard $
 */
package convenience_classes;

public class SubstringBetween {
  // Substring between
  //-----------------------------------------------------------------------
  /**
   * Gets the String that is nested in between two instances of the
   * same String.
   *
   * A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> tag returns <code>null</code>.
   *
   * <pre>
   * StringUtils.substringBetween(null, *)            = null
   * StringUtils.substringBetween("", "")             = ""
   * StringUtils.substringBetween("", "tag")          = null
   * StringUtils.substringBetween("tagabctag", null)  = null
   * StringUtils.substringBetween("tagabctag", "")    = ""
   * StringUtils.substringBetween("tagabctag", "tag") = "abc"
   * </pre>
   *
   * @param str  the String containing the substring, may be null
   * @param tag  the String before and after the substring, may be null
   * @return the substring, <code>null</code> if no match
   * @since 2.0
   */
  public String substringBetween(String str, String tag) {
      return substringBetween(str, tag, tag);
  }

  /**
   * Gets the String that is nested in between two Strings.
   * Only the first match is returned.
   *
   * A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> open/close returns <code>null</code> (no match).
   * An empty ("") open and close returns an empty string.
   *
   * <pre>
   * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
   * StringUtils.substringBetween(null, *, *)          = null
   * StringUtils.substringBetween(*, null, *)          = null
   * StringUtils.substringBetween(*, *, null)          = null
   * StringUtils.substringBetween("", "", "")          = ""
   * StringUtils.substringBetween("", "", "]")         = null
   * StringUtils.substringBetween("", "[", "]")        = null
   * StringUtils.substringBetween("yabcz", "", "")     = ""
   * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
   * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
   * </pre>
   *
   * @param str  the String containing the substring, may be null
   * @param open  the String before the substring, may be null
   * @param close  the String after the substring, may be null
   * @return the substring, <code>null</code> if no match
   * @since 2.0
   */
  public String substringBetween(String str, String open, String close) {
      if (str == null || open == null || close == null) {
          return null;
      }
      int start = str.indexOf(open);
      if (start != -1) {
          int end = str.indexOf(close, start + open.length());
          if (end != -1) {
              return str.substring(start + open.length(), end);
          }
      }
      return null;
  }

  // Empty checks
  //-----------------------------------------------------------------------
  /**
   * Checks if a String is empty ("") or null.
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * NOTE: This method changed in Lang version 2.0.
   * It no longer trims the String.
   * That functionality is available in isBlank().
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is empty or null
   */
  public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
  }
  
}
