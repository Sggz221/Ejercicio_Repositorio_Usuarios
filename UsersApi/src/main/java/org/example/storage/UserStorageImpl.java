package org.example.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.reactivex.rxjava3.core.Single;
import org.example.exceptions.UserException;
import org.example.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class UserStorageImpl implements UserStorage {
    private final Logger logger = LoggerFactory.getLogger(UserStorageImpl.class);

    public UserStorageImpl() {
        logger.info("JsonStorage inicializandose.");
    }

    /**
     * Metodo que exporta a JSON una lista de usuarios.
     * @param file El archivo donde se va a exportar.
     * @param users La lista de usuarios que se quiere exportar.
     */
    @Override
    public Single<Integer> fileWrite(File file, List<User> users) {
        try{
            if(!file.isFile() || !file.exists()) throw new UserException.StorageException("Error, el archivo no existe o no es un archivo.");
            if(!file.getPath().endsWith(".json")) throw new UserException.StorageException("Error, el archivo no es un JSON.");
            // Clase que mapea campos de un objeto a un JSON
            ObjectMapper mapper = new ObjectMapper();
            // Indentacion para que salga como un JSON
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Modulo para poder serializar y deserializar fechas
            mapper.registerModule(new JavaTimeModule());
            mapper.writeValue(file, users);
            logger.info("JSON completado");
            return Single.just(users.size());
        }
        catch (Exception e){
            return Single.error(new UserException.StorageException("Error al escribir datos: " + e.getMessage()));
        }
    }
}
