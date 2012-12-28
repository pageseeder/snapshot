package com.weborganic.snapshot;

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
//    base = "http://dev.pageseeder.com";
    if (base == null) {
      usage("Base URL not specified, use -base");
      return;
    }
    String load = get(args, "-load");
//    load = "test/paths.txt";
    if (load == null) {
      usage("Filelist not specified, use -load");
      return;
    }
    // Optional jsession ID
    String jsessionid = get(args, "-jsessionid");
//    jsessionid= "1e0rpbr0nwlye1mpaw57aeq47q";

    String dir = get(args, "-o");
    if (dir == null) {
      dir = new File("snapshot").getAbsolutePath();
    }

    // Load the list
    List<Resource> resources = load(load);
    Config spec = new Config(base, dir);
    if (jsessionid != null) {
      spec.setJSession(jsessionid);
    }

    // Iterate over
    for (Resource r : resources) {
      URLFetcher page = new URLFetcher(r);
      page.retrieve(spec);
      page = null; // set to death
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
   * @param name the name of the requested option.
   *
   * @return the value if available or <code>null</code>.
   */
  private static String get(String[] args, String name) {
    for (int i = 0; i < args.length; i++) {
      if (name.equals(args[i]) && i < args.length - 1) {
        return args[++i];
      }
    }
    return null;
  }

  /**
   * Loads the configuration file
   *
   * @param file
   */
  private static List<Resource> load(String file) throws IOException {
    List<Resource> paths = new ArrayList<Resource>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    String info = reader.readLine();
    while (info != null) {
      String resource = info.trim();
      if (resource.length() > 0 && resource.indexOf('#') != 0) {
        int s = resource.lastIndexOf(' ');
        Resource r;
        if (s > 0) {
          String path = resource.substring(0, s);
          String method = resource.substring(s+1);
          r = new Resource(path, method);
        } else {
          r = new Resource(resource, "GET");
        }
        paths.add(r);
      }
      info = reader.readLine();
    }
    reader.close();
    return paths;
  }

}
