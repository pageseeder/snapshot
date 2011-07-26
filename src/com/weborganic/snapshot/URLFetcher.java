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
 * Retrieves the page from the Website.  
 * 
 * @author Christophe Lauret
 * @version 26 July 2011
 */
public final class URLFetcher {

  /**
   * The URL to retrieve.
   */
  private final URL _url;

  /**
   * Stylesheets and scripts.
   */
  private final static Pattern LINKS = Pattern.compile("(\\<link[^>]*\\>)|(\\<script[^>]*\\>)");

  /**
   * Stylesheets and scripts.
   */
  private final static Pattern URLS = Pattern.compile("url\\([^)]*\\)");

  /**
   * Creates a new Page for the specified URL
   * 
   * @param url The URL of the page
   * 
   * @throws MalformedURLException
   */
  public URLFetcher(String url) throws MalformedURLException {
    this._url = new URL(url);
  }

  public void retrieve(Config spec) throws IOException {
    // Create the file
    String path = this._url.getPath();
    File target = new File(spec.directory(), path);
    if (path.indexOf('.') > 0) {
      target.getParentFile().mkdirs();
    }

    // No need to process twice
    if (target.exists()) {
      System.out.println("Skipping "+this._url);
    }

    // Start fetching
    System.out.print("Fetching "+this._url);
    HttpURLConnection connection = (HttpURLConnection)this._url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "BerliozSnapshot/1.0");
    connection.connect();

    // Grab the metadata
    String service = connection.getHeaderField("X-Berlioz-Service");
    String mediaType = connection.getHeaderField("Content-Type");
    String encoding = "utf-8";
    int charset = mediaType.indexOf(";charset=");
    if (charset >= 0) {
      encoding = mediaType.substring(charset+9);
      mediaType = mediaType.substring(0, charset);
    }
    if (service != null) {
      System.out.println("-> Service:"+service+" as "+mediaType+" ["+encoding+"]");
    } else {
      System.out.println("-> "+mediaType+" ["+encoding+"]");
    }

    // Grab the content
    InputStream in = connection.getInputStream();

    // Text content that requires processing
    if ("text/html".equals(mediaType) || "text/css".equals(mediaType)) {
      String content = IOUtils.toString(in, encoding);
      connection.disconnect();

      // Parse HTML and fetch scripts and CSS
      if ("text/html".equals(mediaType)) {
        content = processHTML(content, spec, path);
      } else if ("text/css".equals(mediaType)) {
        content = processCSS(content, spec, path);
      }

      // Output to the file
      FileOutputStream fos = new FileOutputStream(target);
      OutputStreamWriter out = new OutputStreamWriter(fos, spec.encoding());
      IOUtils.copy(new StringReader(content), out);
      out.close();

    // Binary content
    } else {
      FileOutputStream fos = new FileOutputStream(target);
      IOUtils.copy(in, fos);
      connection.disconnect();
      fos.close();
    }

  }

  /**
   * Process HTML content
   * 
   * @param content
   * @param spec
   * @param origin
   * @return
   * @throws IOException
   */
  String processHTML(String content, Config spec, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Matcher m = LINKS.matcher(content);
    while (m.find()) {
      String replacement = processLink(m.group(), spec, origin);
      m.appendReplacement(html, replacement);
    }
    m.appendTail(html);
    return html.toString();
  }

  /**
   * Process a link 
   * 
   * @param link
   * @param spec
   * @param origin
   * @return
   * @throws IOException
   */
  String processLink(String link, Config spec, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Pattern p = Pattern.compile("(src|href)=(\"[^\"]*\"|\'[^\']*\')");
    Matcher m = p.matcher(link);
    if (m.find()) {
      String type = m.group(1);
      String location = m.group(2);
      location = unquote(location);
      if (location.startsWith("/")) {
        URLFetcher linkEnd = new URLFetcher(spec.baseURL()+location);
        linkEnd.retrieve(spec);
        String replacement = type+"=\""+toRelativePath(origin, location)+"\"";
        m.appendReplacement(html, replacement);
      } else {
        // No change
        m.appendReplacement(html, m.group());
      }
    }
    m.appendTail(html);
    return html.toString();
  }

  /**
   * Process HTML content
   * 
   * @param content
   * @param spec
   * @param origin
   * @return
   * @throws IOException
   */
  String processCSS(String content, Config spec, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Matcher m = URLS.matcher(content);
    while (m.find()) {
      String replacement = processUrl(m.group(), spec, origin);
      m.appendReplacement(html, replacement);
    }
    m.appendTail(html);
    return html.toString();
  }

  /**
   * Process a link 
   * 
   * @param link
   * @param spec
   * @param origin
   * @return
   * @throws IOException
   */
  String processUrl(String link, Config spec, String origin) throws IOException {
    StringBuffer html = new StringBuffer();
    Pattern p = Pattern.compile("url\\(([^)]*)\\)");
    Matcher m = p.matcher(link);
    if (m.find()) {
      String location = m.group(1);
      location = unquote(location);
      if (location.startsWith("/")) {
        URLFetcher linkEnd = new URLFetcher(spec.baseURL()+location);
        linkEnd.retrieve(spec);
        String replacement = "url("+toRelativePath(origin, location)+")";
        m.appendReplacement(html, replacement);
      } else if (location.startsWith("http://") || location.startsWith("https://")) {
        m.appendReplacement(html, m.group());
      } else {
        String parent = origin.indexOf('/') >= 0? origin.substring(0, origin.lastIndexOf('/'))+"/" : "/";
        URLFetcher linkEnd = new URLFetcher(spec.baseURL()+parent+location);
        linkEnd.retrieve(spec);
        String replacement = "url("+toRelativePath(origin, location)+")";
        m.appendReplacement(html, replacement);
      }
    }
    m.appendTail(html);
    return html.toString();
  }

  /**
   * 
   * @param origin
   * @param target
   * @return
   */
  public static String toRelativePath(String origin, String target) {
    if (origin.startsWith("/") && target.startsWith("/")) {
      return toRelativePath(origin.substring(1), target.substring(1));
    }
    StringBuilder path = new StringBuilder();
    int start = 0;
    while (origin.indexOf('/', start) > 0) {
      path.append("../");
      start = origin.indexOf('/', start)+1;
    }
    path.append(target);
    return path.toString();
  }

  /**
   * Unquote the string if needed.
   * 
   * @param s The string to unquote 
   * @return the unquoted string
   */
  public static String unquote(String s) {
    String unquoted = s;
    if (s.length() > 2) {
      char first = s.charAt(0);
      char last = s.charAt(s.length()-1);
      if (first == last && (first =='\'' || first == '"'))
        unquoted = s.substring(1, s.length()-1);
    }
    return unquoted;
  }

}
