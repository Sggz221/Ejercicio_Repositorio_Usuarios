package org.example.service;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.example.models.User;

import java.io.File;
import java.util.List;

public interface UserService {

    Single<List<User>> getAll();

    Maybe<User> getById(long id);

    Maybe<User> save(User user);

    Maybe<User> update(User user, long id);

    Maybe<User> delete(long id);

    Single<Integer> fileWrite(File file, List<User> users);

}
