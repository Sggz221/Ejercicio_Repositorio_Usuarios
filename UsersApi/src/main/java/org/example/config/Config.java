package org.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config instance;
    private static String resourceName = "config.properties"; // Nombre del recurso a cargar (sobrescribible en tests)
    private final Properties properties;
    private final Logger logger = LoggerFactory.getLogger(Config.class);

    private Config(){
        logger.info("Creando instancia de Config.");
        this.properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                logger.warn("No se encontró el archivo %s. Se usarán valores por defecto.".formatted(resourceName));
                setDefaults();
            } else {
                logger.info("Se cargó el archivo de propiedades correctamente: {}", resourceName);
                properties.load(input);
            }
        } catch ( IOException ex) {
            logger.error("Error al cargar el archivo de propiedades. Se usarán valores por defecto.", ex);
            System.err.println("Error al cargar el archivo de propiedades. Se usarán valores por defecto.");
            setDefaults();
        }
    }
    /**
     * Devuelve la instancia única de configuración.
     *
     * @return Singleton de Config
     */
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Establece el nombre del recurso de properties (solo pruebas). Si es null o blanco, usa "config.properties".
     * @param newResourceName Nombre del recurso a utilizar
     */
    static synchronized void setResourceNameForTests(String newResourceName) {
        resourceName = newResourceName == null || newResourceName.isBlank() ? "config.properties" : newResourceName;
    }

    /**
     * Establece valores por defecto cuando no se puede leer el properties.
     */
    private void setDefaults() {
        properties.setProperty("database.url", "jdbc:h2:mem:users;DB_CLOSE_DELAY=-1");
        properties.setProperty("api.url", "https://jsonplaceholder.typicode.com/");
        properties.setProperty("cache.size", "5");
    }

    /**
     * Obtiene la URL de la base de datos.
     * @return Cadena con la URL
     */
    public String getDatabaseUrl() {
        return properties.getProperty("database.url");
    }

    /**
     * Obtiene el tamaño de la caché de la aplicación.
     *
     * @return tamaño de caché
     */
    // Método para obtener el tamaño de la caché
    public int getCacheSize() {
        return Integer.parseInt(properties.getProperty("cache.size")); // Valor por defecto
    }

    /**
     * Método para obtener la URL de la API
     * @return CAdena con la URL
     */
    public String getApiUrl(){
        return properties.getProperty("api.url");
    }
}
