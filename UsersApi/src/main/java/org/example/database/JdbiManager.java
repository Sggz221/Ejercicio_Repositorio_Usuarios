package org.example.database;

import org.example.config.Config;
import org.example.dao.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class JdbiManager {
    private final Jdbi jdbi;
    private static JdbiManager instance;
    private final Logger logger = LoggerFactory.getLogger(JdbiManager.class);

    /**
     * Constructor privado que crea la BD.
     */
    private JdbiManager() {
        logger.info("Creando instancia de JdbiMangaer y configurando JDBI.");
        String url = Config.getInstance().getDatabaseUrl();

        this.jdbi = Jdbi.create(url);

        jdbi.installPlugin(new SqlObjectPlugin());
        executeSqlScriptFromResources("tables.sql");
    }

    /**
     * Metodo que crea una instancia de JdbiManager única entre hilos.
     * @return JdbiManager siguiendo el pa´tron Singleton.
     */
    public static synchronized JdbiManager getInstance() {
        if (instance == null) {
            instance = new JdbiManager();
        }
        return instance;
    }

    /**
     * Provee un DAO de usuarios.
     */
    public UserDao getUsersDao(){
        return jdbi.onDemand(UserDao.class);
    }

    /**
     * Ejecuta un script SQL desde la carpeta resources.
     * @param resourcePath Ruta del script.
     */
    private void executeSqlScriptFromResources(String resourcePath) {
        logger.debug("Cargando script SQL desde recursos: " + resourcePath);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String script = reader.lines().collect(Collectors.joining("\n"));
            // Ejecuta el script en un solo handle
            jdbi.useHandle(handle -> handle.createScript(script).execute());
        } catch (Exception e) {
            logger.error("Error al ejecutar el script SQL desde recursos: {}", resourcePath, e);
            logger.error("Error: {}", e.getMessage());
        }
    }
}
