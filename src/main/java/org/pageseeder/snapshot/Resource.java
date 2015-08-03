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

/**
 * Represent a resource to load.
 *
 * @author Christophe Lauret
 * @version 28 December 2012
 */
public final class Resource {

  /**
   * The path to the resource.
   */
  private final String _path;

  /**
   * The method to access that resource.
   */
  private final String _method;

  /**
   *
   * @param path   The path to get that resource.
   * @param method The method to access that resource.
   */
  public Resource(String path, String method) {
    this._path = path;
    this._method = method;
  }

  /**
   * @return The path to the resource.
   */
  public String path() {
    return this._path;
  }

  /**
   * Returns the path inserting the JSession ID.
   *
   * @param jsessionid The jsession ID to insert
   * @return The path to the resource with the following jsession attached.
   */
  public String path(String jsessionid) {
    int q = this._path.indexOf('?');
    if (q >=0) return this._path.substring(0, q)+";jsessionid="+jsessionid+this._path.substring(q);
    return this._path+";jsessionid="+jsessionid;
  }

  /**
   * @return The method to access that resource.
   */
  public String method() {
    return this._method;
  }
}
