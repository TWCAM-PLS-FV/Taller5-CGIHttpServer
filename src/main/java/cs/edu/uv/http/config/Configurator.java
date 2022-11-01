package cs.edu.uv.http.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Utility class to read configuration parameters for the application from
 * properties files and from environment.
 * The order to search for properties is:
 * </p>
 * <ul>
 * <li>From a properties file bundled in the jar file</li>
 * <li>From a properties file located in the project folder (if the path is
 * null)</li>
 * <li>From a properties file in the path passed as argument</li>
 * <li>From environment variables</li>
 * </ul>
 */
public class Configurator {
   private HashMap<String, String> config;
   private static Logger log = LoggerFactory.getLogger(Configurator.class);

   public Configurator(List<String> keys, String configFileName, String path) {

      config = new HashMap<String, String>();

      HashMap<String, String> configF = null;
      if (configFileName != null)
         configF = readConfigFile(keys, configFileName, path);

      HashMap<String, String> configE = readConfigEnv(keys);

      if (configF != null)
         configF.forEach((k, v) -> config.put(k, v));
      
      configE.forEach((k, v) -> config.put(k, v));

      log.debug("== Final service configuration");
      config.forEach((k, v) -> log.debug(k + ": " + v));
   }

   public int getRequiredIntProperty(String name) throws RuntimeException {
      String val = config.get(name);
      if (val == null)
         throw new RuntimeException("Required property " + name + " not found");
      else
         return Integer.parseInt(val);
   }

   public int getIntProperty(String name, int def) {
      String val = config.get(name);
      return val == null ? def : Integer.parseInt(val);
   }

   public boolean getBooleanProperty(String name, boolean def) {
      String val = config.get(name);
      return val == null ? def : Boolean.parseBoolean(val);
   }

   public double getRequiredDoubleProperty(String name) throws RuntimeException {
      String val = config.get(name);
      if (val == null)
         throw new RuntimeException("Required property " + name + " not found");
      else
         return Double.parseDouble(val);
   }

   public double getDoubleProperty(String name, double def) {
      String val = config.get(name);
      return val == null ? def : Double.parseDouble(val);
   }

   public String getProperty(String name, String def) {
      String val = config.get(name);
      return val == null ? def : val;
   }

   public String getRequiredProperty(String name) throws RuntimeException {
      String val = config.get(name);
      if (val == null)
         throw new RuntimeException("Required property " + name + " not found");
      else
         return val;
   }

   /**
    * Reads properties from:
    * <ul>
    * <li>From a properties file bundled in the jar file</li>
    * <li>From a properties file located in /config folder</li>
    * <li>From a properties file located in the project folder</li>
    * </ul>
    *
    * @param keys     the names of the variables to read
    * @param fileName the name of the file where properties are stored
    * @param log      wether to show the properties and the values in System.out or
    *                 not
    * @return the properties and their values in a hashmap
    */
   private static HashMap<String, String> readConfigFile(List<String> keys, String configFileName, String path) {
      HashMap<String, String> config = new HashMap<String, String>();
      if (configFileName != null) {
         InputStream is = Configurator.class.getClassLoader().getResourceAsStream(configFileName);
         Properties p1 = new Properties();
         boolean config1 = false;
         try {
            p1.load(is);
            log.info("Found config file in jar file");
            config1 = true;
         } catch (Exception ex) {
         }

         // Properties in 
         Properties p2 = new Properties();
         boolean config2 = false;
         
         Properties p3 = new Properties();
         boolean config3 = false;
         if (path == null)
            try {
               // Try to find the file in the folder of the app
               File f = new File(configFileName);
               p2.load(new FileInputStream(f));
               log.info("Found config file in {}", f.getAbsolutePath());
               config2 = true;
            } catch (Exception ex) {
            }
         else
            try {
               // Try to find the file in the given path
               File f = new File(path +  "/" + configFileName);
               p3.load(new FileInputStream(f));
               log.info("Found config file in {}", f.getAbsolutePath());
               config3 = true;
            } catch (Exception ex) {
            }

         for (String k : keys) {
            // Config in jar
            if (config1) {
               String aux = (String) p1.get(k);
               if (aux != null) {
                  config.put(k, aux.trim());
                  log.debug("Property {} read from bundled config file: {}", k, aux);
               }
            }
            // Config in app folder
            if (config2) {
               String aux = (String) p2.get(k);
               if (aux != null) {
                  config.put(k, aux.trim());
                  log.debug("Property {} read from {}: {}.", k, configFileName, aux);
               }
            }
            // Config in given folder
            if (config3) {
               String aux = (String) p3.get(k);
               if (aux != null) {
                  config.put(k, aux.trim());
                  log.debug("Property {} read from {}: {}.", k,path+"/"+configFileName,aux);
               }
            }
         }
      }
      return config;
   }


   /**
    * Reads properties from environment variables
    * 
    * @param keys
    * @param log
    * @return
    */
   private static HashMap<String, String> readConfigEnv(List<String> keys) {
      HashMap<String, String> config = new HashMap<String, String>();
      for (String k : keys) {
         // Environment variables
         String aux = System.getenv(k);
         if (aux != null) {
            config.put(k, aux.trim());
            log.debug("Property {} read from environment: {}.", k, aux);
         }
      }
      return config;
   }
}

