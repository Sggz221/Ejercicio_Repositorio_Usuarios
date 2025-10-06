package org.example.dao;


import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la clase UserEntity con las operaciones CRUD sobre la base de datos embebida
 */
@RegisterConstructorMapper(UserEntity.class)
public interface UserDao {

    /**
     * @return Lista de usuarios de la BD
     */
    @SqlQuery("SELECT * FROM users")
    List<UserEntity> findAll();

    /**
     * Busca un usuario en base a un ID
     * @param id El id del usuario a buscar
     * @return El usuario en caso correcto, null en caso de que no lo encuentre
     */
    @SqlQuery("SELECT * FROM users WHERE id = :id")
    Optional<UserEntity> findById(@Bind("id") long id); // Optinal porque puede dar null

    /**
     * Guarda un usuario en la BD
     * @param userEntity
     * @return 0 en caso incorrecto, 1 en caso correcto
     */
    @SqlUpdate("INSERT INTO users (id, name, username, email, created_at, updated_at) VALUES(:id, :name, :username, :email, :createdAt, :updatedAt)")
    int save(@BindBean UserEntity userEntity);

    /**
     * Actualiza un usuario en la BD
     * @param userEntity
     * @param identificator El id que queremos actualizar
     * @return 0 en caso incorrecto, 1 en caso correcto
     */
    @SqlUpdate("UPDATE users SET id = :identificator, name = :name, username = :username, email = :email, created_at = :createdAt, updated_at = :updatedAt WHERE id = :identificator")
    int update(@BindBean UserEntity userEntity, @Bind("id") Long identificator);

    /**
     * Borra un usuario en la BD en base a un ID
     * @param id
     * @return 0 en caso incorrecto, 1 en caso correcto
     */
    @SqlUpdate("DELETE * FROM users WHERE id = :id")
    int delete(@Bind("id") Long id);

    /**
     * Borra todos los usuarios de la base de datos.
     * @return
     */
    @SqlUpdate("DELETE * FROM users")
    int deleteAll();
}
