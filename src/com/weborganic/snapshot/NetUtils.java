package com.weborganic.snapshot;

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
