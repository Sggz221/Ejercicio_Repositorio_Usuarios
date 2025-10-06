package org.example.mapper;

import org.example.dao.UserEntity;
import org.example.models.User;
import org.example.rest.UserApiEntity;

/**
 * Clase que permite transformasr un objeto User en un objeto de la clase UserEntity .
 */
public class UserMapper {

    /**
     * Proporcionando un UserEntity, devuelve un objeto User equivalente.
     * @param userEntity
     * @return User
     */
    public static User toUser(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getUsername(),
                userEntity.getEmail()
        );
    }

    /**
     * Proporcionando un User, devuelve un objeto UserEntity equivalente.
     * @param user
     * @return UserEntity
     */
    public static UserEntity toUserEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail()
        );
    }

    /**
     * Proporcionando un UserApiEntity, devuelve un objeto User equivalente.
     * @param userApiEntity
     * @return User
     */
    public static User toUser(UserApiEntity userApiEntity) {
        return new User(
                userApiEntity.getId(),
                userApiEntity.getName(),
                userApiEntity.getUsername(),
                userApiEntity.getEmail()
        );
    }

    /**
     * Proporcionando un User, devuelve un objeto UserEntity equivalente.
     * @param user
     * @return UserEntity
     */
    public static UserApiEntity toUserApiEntity(User user) {
        return new UserApiEntity(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
