/*
 * Copyright 2010-2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.snapshot;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class NetUtils {


  /**
   *
   * @param headers
   */
  public static void printHeaders(Map<String, List<String>> headers) {
    for (Entry<String, List<String>> h : headers.entrySet()) {
      String name = h.getKey();
      for (String value : h.getValue()) {
        if (name != null) {
          System.err.println(name + ":\t" + value);
        } else {
          System.err.println(value);
        }
      }
    }
  }

}
