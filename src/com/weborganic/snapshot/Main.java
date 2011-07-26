package com.weborganic.snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes a snapshot of a Berlioz Application
 * 
 * @author Christophe Lauret
 * @version 26 July 2011
 */
public class Main {

  private Main() {
  }

  public static void main(String[] args) throws IOException {
    String base = get(args, "-base");
    if (base == null) {
      usage("Base URL not specified, use -base");
      return;
//      base = "http://oauthserver.weborganic.org:8099";
    }
    String load = get(args, "-load");
    if (load == null) {
      usage("Filelist not specified, use -load");
      return;
//      load = "paths.txt";
    }
    String dir = get(args, "-o");
    if (dir == null) dir = new File("snapshot").getAbsolutePath();

    // Load the list
    List<String> paths = load(load);
    Config spec = new Config(base, dir);

    // Iterate over
    for (String p : paths) {
      URLFetcher page = new URLFetcher(base + p);
      page.retrieve(spec);
    }

  }

  /**
   * Displays the usage of this class on System.err.
   * 
   * @param message Any message (optional)
   */
  public static void usage(String message) {
    if (message != null) {
      System.err.println(message);
    }
    System.err.println("Snapshot");
    System.err.println("Usage: java -jar wi-snapshot.jar");
    System.err.println("Options");
    System.err.println("  -load [filelist] -base [baseurl] -o [outputdir]");
  }

  /**
   * Returns the single value for the specified option if defined.
   * 
   * @param options the matrix of command line options.
   * @param name    the name of the requested option.
   * 
   * @return the value if available or <code>null</code>.
   */
  private static String get(String[] args, String name) {
    for (int i = 0; i < args.length; i++) {
      if (name.equals(args[i]) && i < args.length -1) return args[++i];
    }
    return null;
  }

  /**
   * 
   * @param file
   */
  private static List<String> load(String file) throws IOException {
    List<String> paths = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    String path = reader.readLine();
    while (path != null) {
      paths.add(path.trim());
      path = reader.readLine();
    }
    return paths;
  }

}
