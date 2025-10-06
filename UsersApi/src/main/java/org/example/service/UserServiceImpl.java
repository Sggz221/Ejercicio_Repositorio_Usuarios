package org.example.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import org.example.exceptions.UserException;
import org.example.models.User;
import org.example.repository.UserRemoteRepositoryImpl;
import org.example.repository.UserRepositoryImpl;
import org.example.storage.UserStorageImpl;
import org.example.validator.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepositoryImpl localRepository;
    private final UserRemoteRepositoryImpl remoteRepository;
    private final UserStorageImpl userStorage;
    private final Cache<Long, User> userCache;

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Single<List<User>> getAll() {
        logger.info("Obteniendo todos los usuarios");
        // Obtenemos todos los usuarios del repositorio local
        return localRepository.getAll()
                .flatMap(users ->{
                    // Si hay usuarios en el local los devolvemos
                    if (!users.isEmpty()) {
                        return Single.just(users);
                    }
                    else {
                        return remoteRepository.getAll().flatMap(remoteUsers ->{
                            if (!remoteUsers.isEmpty()) {
                                remoteUsers.forEach(localRepository::save);
                            }
                            return Single.just(remoteUsers);
                        });
                    }
                }).subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<User> getById(long id) {
        var user = userCache.getIfPresent(id);
        // Si no es nulo, se devuelve dde la cache
        if(user != null) {
            return Maybe.just(user);
        }
        // Se busca en el repositorio local
        return localRepository.getById(id).doOnSuccess(u -> {
            // Si se encuentra en el local, se guarda en la cache
            userCache.put(id, u);
        }).switchIfEmpty(
                // Si no se ha encontrado en el local, se busca en el remoto
                remoteRepository.getById(id).doOnSuccess(u -> {
                    // Si se encuentra en el remoto, se guarda en la cache y en el repositorio local
                    userCache.put(id, u);
                    localRepository.save(u);
                })
                // Finalmente, si no estaba en ningun lado se devuelve un error
        ).switchIfEmpty(Maybe.error(new UserException("No existe el usuario con el id " + id)))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<User> save(User user) {
        logger.info("Guardando el usuario");
        try {
            UserValidator.validate(user);
            // Se guarda el usuario en la API
            return remoteRepository.save(user).doOnSuccess(u -> {
                userCache.put(u.getId(), u);
                localRepository.save(u);
            }).doOnError(e -> {
                logger.error(e.getMessage());
            });
        }
        catch (Exception e){
            return Maybe.error(e);
        }
    }

    @Override
    public Maybe<User> update(User user, long id) {
        logger.info("Actualizando el usuario con ID: " + id);
        // Se intenta actualizar en la API
        try {
            UserValidator.validate(user);
            return remoteRepository.update(user, id).doOnSuccess(updated -> {
                // Si se actualiza correctamente se actualiza de la cache y de la BD
                userCache.put(id, updated);
                localRepository.update(updated, id);
                //Si no se actualiza se devuelve un error
            }).switchIfEmpty(Maybe.error(new UserException("No existe el usuario con el id " + id))).subscribeOn(Schedulers.io());
        }
        catch (Exception e){
            return Maybe.error(e);
        }
    }

    @Override
    public Maybe<User> delete(long id) {
        logger.info("Eliminando el usuario con ID: " + id);
        // Se intenta borrar el usuario de la API
        return remoteRepository.delete(id).doOnSuccess(deleted -> {
            // Si se borra correctamente, tambien se borra de la cache y de la BD
            userCache.invalidate(id);
            localRepository.delete(id);
            logger.info("Eliminado correctamente el usuario con ID: " + id);
            // Si no, se devuelve un error
        }).switchIfEmpty(Maybe.error(new UserException("No existe el usuario con id " + id)))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Integer> fileWrite(File file, List<User> users) {
        return userStorage.fileWrite(file, users).doOnError(e -> {
            logger.error(e.getMessage());
        }).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> refreshLocalRepository() {
        logger.info("Sincronizando la base de datos embebida con la API.");
        localRepository.deleteAll();
        return remoteRepository.getAll()
                .map(users -> {
                    for(User user : users) {
                        try {
                            UserValidator.validate(user);
                            localRepository.save(user);
                        }
                        catch (Exception e){
                            logger.error(e.getMessage());
                        }
                    }
                    logger.info("Base de datos local sincronizada con exito");
                    return true;
                })
                .onErrorReturn(error ->{
                    logger.error(error.getMessage());
                    return false;
                }).subscribeOn(Schedulers.io());
    }
}
