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
 * The configuration for snapshot.
 *
 * @author Christophe Lauret
 * @version 28 December 2012
 */
public final class Config {

  /**
   * The base URL ([scheme]://[host])
   */
  private final String _baseurl;

  /**
   * The base directory where files are stored.
   */
  private final String _basedir;

  /**
   * The encoding to read the files.
   */
  private final String _encoding;

  /**
   * The jsession ID (optional)
   */
  private String _jsession;


  public Config(String baseurl, String dir) {
    this._baseurl = baseurl;
    this._basedir = dir;
    this._encoding = "utf-8";
  }

  /**
   * The directory where the files should be stored.
   *
   * @return The directory where the files should be stored.
   */
  public String directory() {
    return this._basedir;
  }

  /**
   * The Base URL of the website to snapshot.
   *
   * @return The Base URL of the website to snapshot.
   */
  public String baseURL() {
    return this._baseurl;
  }

  /**
   * The encoding to use for the files to save.
   *
   * @return "utf-8"
   */
  public String encoding() {
    return this._encoding;
  }

  public String getJSession() {
    return this._jsession;
  }

  public void setJSession(String jsession) {
    this._jsession = jsession;
  }
}
