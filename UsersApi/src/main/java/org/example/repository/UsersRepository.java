package org.example.repository;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.example.models.User;

import java.util.List;

public interface UsersRepository {

    /**
     * Método que devuelve una lista de usuarios del repositorio.
     * @return La lista de usuarios.
     */
    Single<List<User>> getAll();

    /**
     * Método que devuelve un usuario o null en caso de no encontrarlo en base a un ID.
     * @return Optional del usuairo
     */
    Maybe<User> getById(Long id);

    /**
     * Método que guarda un usuario en el repositorio.
     * @return El usuario guardado
     */
    Maybe<User> save(User user);

    /**
     * Método que elimina un usuario del repositorio.
     * @return Optional del usuario.
     */
    Maybe<User> delete(Long id);

    /**
     * Método que actualiza un usuario del repostiorio.
     * @return El Optional del usuario actualizado
     */
    Maybe<User> update(User user, Long id);
}
