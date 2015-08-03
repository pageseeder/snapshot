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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utilities for IO operations.
 *
 * <p>
 * Portions of this code were taken from Apache's CommonsIO library licenced under "Apache Licence 2.0".
 *
 * @author Christophe Lauret
 * @version 26 July 2011
 */
public class IOUtils {

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  /**
   * Copy bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   *
   * @return the number of bytes copied
   *
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   */
  public static long copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Copy chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   *
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   *
   * @return the number of characters copied
   *
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   */
  public static long copy(Reader input, Writer output) throws IOException {
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Get the contents of an <code>InputStream</code> as a String using the specified character encoding.
   * <p>
   * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param encoding the encoding to use, null means platform default
   * @return the requested String
   * @throws NullPointerException if the input is null
   * @throws IOException if an I/O error occurs
   */
  public static String toString(InputStream input, String encoding) throws IOException {
    StringWriter sw = new StringWriter();
    InputStreamReader reader = new InputStreamReader(input, encoding);
    copy(reader, sw);
    return sw.toString();
  }

}
