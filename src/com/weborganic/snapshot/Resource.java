package com.weborganic.snapshot;

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
    if (q >=0) {
      return this._path.substring(0, q)+";jsessionid="+jsessionid+this._path.substring(q);
    }
    return this._path+";jsessionid="+jsessionid;
  }

  /**
   * @return The method to access that resource.
   */
  public String method() {
    return this._method;
  }
}
