package org.example.repository;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.example.dao.UserDao;
import org.example.dao.UserEntity;
import org.example.mapper.UserMapper;
import org.example.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Clase que representa el repositorio que se conectacon la base de datos embebida.
 */
@AllArgsConstructor
public class UserRepositoryImpl implements UsersRepository{
    private UserDao dao;
    private final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    /**
     * Obtiene toda la lista de usuarios de la base de datos en forma de Single para reactividad.
     * @return
     */
    @Override
    public Single<List<User>> getAll() {
        logger.info("Obteninendo todos los usuarios");
        try {
            List<User> userList = dao.findAll().stream().map(UserMapper::toUser).toList();
            return Single.just(userList);
        }
        catch (Exception e) {
            return Single.error(e);
        }
    }

    /**
     * Obtiene un usuario en base a un ID
     * @param id
     * @return
     */
    @Override
    public Maybe<User> getById(Long id) {
        logger.info("Obteniendo usuario con id: " + id);
        try {
            // Obtenemos el UserEntity
            Optional<UserEntity> userEntity = dao.findById(id);
            // Si está presente, lo mapeamos a usuario y lo devolvemos con el maybe.
            if(userEntity.isPresent()) {
                return Maybe.just(UserMapper.toUser(userEntity.get()));
            }
            // Si no está presente, devolvemos el Maybe vacío.
            return Maybe.empty();
        }
        catch (Exception e) {
            return Maybe.error(e);
        }
    }

    /**
     * Inserta un usuario en la BD embebida
     * @param user
     * @return
     */
    @Override
    public Maybe<User> save(User user) {
        logger.info("Guardando usuario: " + user.toString());
        try {
            UserEntity usuariotoSave = UserMapper.toUserEntity(user);
            // Si el numero de filas afectadas es mayor a cero, se ha guardado correctamente
            if(dao.save(usuariotoSave) > 0){
                return Maybe.just(user);
            }
            else {
                return Maybe.empty();
            }
        }
        catch (Exception e) {
            return Maybe.error(e);
        }
    }

    /**
     * Elimina un usuario en base a  un ID de la base de datos embebeida o devuelve un optional vacio en caso de no existir
     * @param id
     * @return
     */
    @Override
    public Maybe<User> delete(Long id) {
        logger.info("Eliminando usuario con id: " + id);
        try {
            Optional<UserEntity> oldUsuario = dao.findById(id);
            // SI esta presente y el numero de filas afectadas es mayor a 0 se ha borrado correctamente
            if(oldUsuario.isPresent() && dao.delete(id) > 0){
                return Maybe.just(dao.findById(id).map(UserMapper::toUser).get());
            }
            else return Maybe.empty();
        }
        catch (Exception e) {
            return Maybe.error(e);
        }
    }

    /**
     * Dado un usuario actualizado por parametro y un ID, lo busca en la BD, y si existe, se actualizan los datos. Por el contrario, se devuelve un optional vacio.
     * @param user
     * @param id
     * @return
     */
    @Override
    public Maybe<User> update(User user, Long id) {
        logger.info("Actualizando usuario con id: " + id);
        try {
            UserEntity usuarioToUpdate = UserMapper.toUserEntity(user);
            Optional<UserEntity> oldUsuario = dao.findById(id);
            // SI esta presente y el numero de filas afectadas es mayor a 0 se ha actualizado correctamente
            if(oldUsuario.isPresent() && dao.update(usuarioToUpdate ,id) > 0){
                return Maybe.just(user);
            }
            else return Maybe.empty();
        }
        catch (Exception e) {
            return Maybe.error(e);
        }
    }

    public Single<Integer> deleteAll() {
        try {
            return Single.just(dao.deleteAll());
        }
        catch (Exception e) {
            return Single.error(e);
        }
    }
}
