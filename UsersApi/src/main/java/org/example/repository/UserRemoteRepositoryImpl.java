package org.example.repository;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.example.exceptions.UserException;
import org.example.mapper.UserMapper;
import org.example.models.User;
import org.example.rest.UsersApiDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class UserRemoteRepositoryImpl implements UsersRepository {
    private final UsersApiDao usersApiDao;
    private final Logger logger = LoggerFactory.getLogger(UserRemoteRepositoryImpl.class);

    @Override
    public Single<List<User>> getAll() {
        logger.info("Obteniendo todos los usuarios de la API");
        try {
            var response = usersApiDao.getAll().execute();
            if (!response.isSuccessful()) throw new UserException.ApiException("Error al obtener usuarios de la API");
            if(response.body() == null) return Single.just( new ArrayList<>());
            return Single.just(response.body().stream().map(UserMapper::toUser).toList());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Single.error(e);
        }
    }

    @Override
    public Maybe<User> getById(Long id) {
        logger.info("Obteniendo usuario por ID: " + id);
        try {
            var response = usersApiDao.getById(id).execute();
            if (!response.isSuccessful()) {
                if(response.code() == 404) throw new UserException.ApiException("El usuario no existe (404)");
                else throw new UserException.ApiException("Error al obtener usuario por ID: " + id + ". Error: " + response.code());
            }
            return Maybe.just(UserMapper.toUser(response.body()));
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return Maybe.empty();
        }
    }

    @Override
    public Maybe<User> save(User user) {
        logger.info("Guardando usuario" + user);
        try {
            var response = usersApiDao.create(UserMapper.toUserApiEntity(user)).execute();
            if (!response.isSuccessful()) throw new UserException.ApiException("Error al guardar usuario. Error: " + response.code());
            return Maybe.just(UserMapper.toUser(response.body()));
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return Maybe.error(e);
        }
    }

    @Override
    public Maybe<User> delete(Long id) {
        logger.info("Eliminando usuario por ID: " + id);
        try {
            var user = usersApiDao.getById(id).execute();
            if(!user.isSuccessful()) {
                if(user.code() == 404) throw new UserException.ApiException("El usuario no existe (404)");
                else throw new UserException.ApiException("Error al buscar el usuario. Error: " + user.code());
            }
            var deleted = usersApiDao.delete(id).execute();
            if (!deleted.isSuccessful()) throw new UserException.ApiException("Error al eliminar usuario. Error: " + deleted.code());
            return Maybe.just(UserMapper.toUser(user.body()));
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return Maybe.empty();
        }
    }

    @Override
    public Maybe<User> update(User user, Long id) {
        logger.info("Actualizando usuario por ID: " + id + "Nuevo usuario: " + user);
        try {
            var userById = usersApiDao.getById(id).execute(); //Cambio el nombre porque el parametro ya se llama user
            if (!userById.isSuccessful()) {
                if (userById.code() == 404) throw new UserException.ApiException("El usuario no existe (404)");
                else throw new UserException.ApiException("Error al buscar el usuario. Error: " + userById.code());
            }
            var response = usersApiDao.update(id, UserMapper.toUserApiEntity(user)).execute();
            if (!response.isSuccessful()) throw new UserException.ApiException("Error al actualizar usuario. Error: " + response.code());
            return Maybe.just(UserMapper.toUser(response.body()));
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return Maybe.empty();
        }
    }
}
