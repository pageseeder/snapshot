package com.weborganic.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves the page from the Website and its associated resources.
 *
 * @author Christophe Lauret
 * @version 26 July 2011
 */
public final class URLFetcher {

  /**
   * The URL to retrieve.
   */
  private final Resource _resource;

  /**
   * Stylesheets, scripts, images and regular links.
   */
  private final static Pattern LINKS = Pattern.compile("(\\<link [^>]*\\>)|(\\<script [^>]*\\>)|(\\<img [^>]*\\>)|(\\<a [^>]*\\>)");

  /**
   * Stylesheets and scripts.
   */
  private final static Pattern URLS = Pattern.compile("url\\([^)]*\\)");

  /**
   * Creates a new Page for the specified URL
   *
   * @param url The URL of the page
   *
   * @throws MalformedURLException If the URL is not valid.
   */
  public URLFetcher(Resource resource) throws MalformedURLException {
    this._resource = resource;
  }

  /**
   * Retrieves resource corresponding to this URL and other associated resources (links, images, etc...)
   *
   * @param config The snapshot configuration.
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  public void retrieve(Config config) throws IOException {
    URL url = new URL(config.baseURL() + this._resource.path(config.getJSession()));
    // Create the file
    String path = url.getPath();
    String query = url.getQuery();

    String cleanpath = path;
    if (path.indexOf(";jsessionid") > 0) {
      String ext = getExtension(this._resource.path());
      cleanpath = path.substring(0, path.indexOf(";jsessionid"));
//      if (!(ext.equals("css") || ext.equals("js") || ext.equals("png") || ext.equals("gif") || ext.equals("jpg"))) {
//        cleanpath = cleanpath + ";auth";
//      }
    }
    File file = new File(config.directory(), cleanpath);
    if (path.indexOf('.') > 0) {
      file.getParentFile().mkdirs();
    }

    // add query to the filename
    if (query != null) {
      StringBuffer filename = new StringBuffer();
      filename.append(file.getName().substring(0, file.getName().lastIndexOf(".")));
      for (String q : query.split("&")) {
        // Ignore berlioz parameters (reload and bundle)
        if (!q.startsWith("berlioz-"))
          filename.append("{;" + q + "}");
      }
      filename.append(file.getName().substring(file.getName().lastIndexOf(".")));
      file = new File(file.getParent(), filename.toString());
    }

    // No need to process twice
    if (file.exists()) {
      System.out.println("Skipping " + url);
    }

    // Start fetching
    System.out.print("Fetching " + url);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "WeborganicSnapshot/1.0");
    connection.connect();

    // Response code
    int code = connection.getResponseCode();
    if (code == 404 && isStubbable(file)) {
      createStub(file, config);
    } else {
      retrieveContent(connection, file, config, path);
    }

  }

  /**
   * Retrieves the content from the connection.
   */
  private void retrieveContent(HttpURLConnection connection, File file, Config config, String path) throws IOException {
    // Grab the metadata
    String service = connection.getHeaderField("X-Berlioz-Service");
    String mediaType = connection.getHeaderField("Content-Type");
    String encoding = "utf-8";
    int charset = mediaType.indexOf(";charset=");
    if (charset >= 0) {
      encoding = mediaType.substring(charset + 9);
      mediaType = mediaType.substring(0, charset);
    }
    if (service != null) {
      System.out.println("-> Service:" + service + " as " + mediaType + " [" + encoding + "]");
    } else {
      System.out.println("-> " + mediaType + " [" + encoding + "]");
    }

    // Grab the content
    InputStream in = connection.getInputStream();

    // Text content that requires processing
    if ("text/html".equals(mediaType) || "text/css".equals(mediaType)) {
      String content = IOUtils.toString(in, encoding);
      connection.disconnect();

      // Parse HTML and fetch scripts and CSS
      if ("text/html".equals(mediaType)) {
        content = processHTML(content, config, path);
      } else if ("text/css".equals(mediaType)) {
        content = processCSS(content, config, path);
      }

      // Output to the file
      FileOutputStream fos = new FileOutputStream(file);
      OutputStreamWriter out = new OutputStreamWriter(fos, config.encoding());
      IOUtils.copy(new StringReader(content), out);
      out.close();

      // Binary content
    } else {
      FileOutputStream fos = new FileOutputStream(file);
      IOUtils.copy(in, fos);
      connection.disconnect();
      fos.close();
    }
  }

  /**
   * Retrieves the content from the connection.
   */
  private void createStub(File file, Config config) throws IOException {
    String name = file.getName();
    StringBuilder stub = new StringBuilder();
    if (name.endsWith(".css")) {
      stub.append("/**\n");
      stub.append(" * This is a CSS Stub\n");
      stub.append(" */\n");
    } else if (name.endsWith(".js")) {
      stub.append("/**\n");
      stub.append(" * This is a JavaScript Stub\n");
      stub.append(" */\n");
    }

    // Output to the file
    FileOutputStream fos = new FileOutputStream(file);
    OutputStreamWriter out = new OutputStreamWriter(fos, config.encoding());
    IOUtils.copy(new StringReader(stub.toString()), out);
    out.close();
  }

  /**
   * Retrieves the specified URL for the given config.
   *
   * <p>
   * Same as:
   *
   * <pre>
   * URLFetcher linkEnd = new URLFetcher(url);
   * linkEnd.retrieve(config);
   * </pre>
   *
   * @param url The URL to retrieve.
   * @param config The SnapShot config.
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  public static void retrieve(String path, Config config) throws IOException {
    URLFetcher linkEnd = new URLFetcher(new Resource(path, "GET"));
    linkEnd.retrieve(config);
  }

  /**
   * Indicates whether a stub can be created (for example for JavaScript or CSS).
   *
   * @return <code>true</code> if it can; <code>false</code> otherwise.
   */
  private static boolean isStubbable(File file) {
    String name = file.getName();
    if (name.endsWith(".css")) {
      return true;
    }
    if (name.endsWith(".js")) {
      return true;
    }
    return false;
  }

  // HTML Processing ==============================================================================

  /**
   * Process HTML content.
   *
   * <p>
   * This method rewrites tags and fetches associated resources.
   *
   * @param content The entire HTML content.
   * @param config The snapshot configuration.
   * @param origin The path to this HTML file.
   *
   * @return The rewritten content.
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  private String processHTML(String content, Config config, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Matcher m = LINKS.matcher(content);
    while (m.find()) {
      String replacement = processLink(m.group(), config, origin);
      m.appendReplacement(html, replacement);
    }
    m.appendTail(html);
    return html.toString();
  }

  /**
   * Process an HTML linked item (image, script, styles, etc...)
   *
   * <p>
   * This method rewrites, but does not follow regular links.
   *
   * @param tag The complete matching tag (opening element).
   * @param config The snapshot configuration.
   * @param origin The path to HTML file.
   *
   * @return The rewritten tag
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  private String processLink(String tag, Config config, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Pattern p = Pattern.compile("(src|href)=(\"[^\"]*\"|\'[^\']*\')");
    Matcher m = p.matcher(tag);
    if (m.find()) {
      String type = m.group(1);
      String location = m.group(2);
      location = unquote(location);

      if (location.startsWith("/")) {
        // Fetch images, scripts and styles (but do not follow links <a>)
        if (!tag.startsWith("<a ")) {
          URLFetcher.retrieve(location, config);
        }
        // Rewrite the absolute paths
        m.appendReplacement(html, type + "=\"" + toRelativePath(origin, location) + "\"");

      } else if (location.startsWith("http://")
          || location.startsWith("https://")
          || location.startsWith("#")) {
        // Ignore full path and internal links
        m.appendReplacement(html, m.group());

      } else {
        String parent = origin.indexOf('/') >= 0 ? origin.substring(0, origin.lastIndexOf('/')) + "/" : "/";
        // Fetch images, scripts and styles (but do not follow links <a>)
        if (!tag.startsWith("<a ")) {
          URLFetcher.retrieve(parent + location, config);
        }
        // Rewrite relative paths
        m.appendReplacement(html, type + "=\"" + toRelativePath(origin, parent + location) + "\"");
      }
    }
    m.appendTail(html);
    return html.toString();
  }

  // HTML Processing ==============================================================================

  /**
   * Process CSS content.
   *
   * <p>
   * This method will rewrite <code>url()</code> references.
   *
   * @param content The CSS content
   * @param config The snapshot configuration.
   * @param origin The path to the CSS file.
   *
   * @return the updated CSS content.
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  String processCSS(String content, Config config, String origin) throws IOException {
    StringBuffer css = null;
    Matcher m = URLS.matcher(content);
    while (m.find()) {
      if (css == null) {
        css = new StringBuffer();
      }
      m.appendReplacement(css, processUrl(m.group(), config, origin));
    }
    if (css != null) {
      m.appendTail(css);
      return css.toString();
    } else {
      return content;
    }
  }

  /**
   * Process a linked item in a CSS file (most likely an image or another CSS)
   *
   * @param link The complete matching tag.
   * @param config The snapshot configuration.
   * @param origin The path to the CSS file.
   *
   * @return The rewritten link
   *
   * @throws IOException In case of an unrecoverable and unexpected I/O or network error.
   */
  String processUrl(String link, Config config, String origin) throws IOException {
    StringBuffer css = new StringBuffer();
    Pattern p = Pattern.compile("url\\(([^)]*)\\)");
    Matcher m = p.matcher(link);
    if (m.find()) {
      String location = m.group(1);
      location = unquote(location);
      // Rewrite the absolute paths
      if (location.startsWith("/")) {
        URLFetcher.retrieve(location, config);
        m.appendReplacement(css, "url(" + toRelativePath(origin, location) + ")");

        // Ignore full path and internal links
      } else if (location.startsWith("http://")
          || location.startsWith("https://")
          || location.startsWith("#")) {
        m.appendReplacement(css, m.group());

        // Rewrite relative paths
      } else {
        String parent = origin.indexOf('/') >= 0 ? origin.substring(0, origin.lastIndexOf('/')) + "/" : "/";
        URLFetcher.retrieve(parent + location, config);
        m.appendReplacement(css, "url(" + toRelativePath(origin, parent + location) + ")");
      }
    }
    m.appendTail(css);
    return css.toString();
  }

  /**
   * Compute the relative path from the specified origin to the specified target.
   *
   * @param origin The path to the origin.
   * @param target The path to the target.
   *
   * @return the relative path.
   */
  public static String toRelativePath(String origin, String target) {
    if (origin.startsWith("/") && target.startsWith("/")) {
      return toRelativePath(origin.substring(1), target.substring(1));
    }
    StringBuilder path = new StringBuilder();
    int start = 0;
    while (origin.indexOf('/', start) > 0) {
      path.append("../");
      start = origin.indexOf('/', start) + 1;
    }
    path.append(target);
    return path.toString();
  }

  /**
   * Removes the quotes from the string if needed.
   *
   * @param s The string to unquote
   * @return the unquoted string
   */
  public static String unquote(String s) {
    String unquoted = s;
    if (s.length() > 2) {
      char first = s.charAt(0);
      char last = s.charAt(s.length() - 1);
      if (first == last && (first == '\'' || first == '"')) {
        unquoted = s.substring(1, s.length() - 1);
      }
    }
    return unquoted;
  }


  /**
   *
   */
  public static String getExtension(String path) {
    int dot = path.lastIndexOf('.');
    return dot != -1? path.substring(dot+1) : "";
  }

}
