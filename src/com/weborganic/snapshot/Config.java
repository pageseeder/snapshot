package com.weborganic.snapshot;

/**
 *
 */
public final class Config {

  private final String _baseurl;
  
  private final String _basedir;
  
  private final String _encoding;

  
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
    return _basedir;
  }

  /**
   * The Base URL of the website to snapshot.
   * 
   * @return The Base URL of the website to snapshot.
   */
  public String baseURL() {
    return _baseurl;
  }

  /**
   * The encoding to use for the files to save.
   * 
   * @return "utf-8"
   */
  public String encoding() {
    return _encoding;
  }

}
