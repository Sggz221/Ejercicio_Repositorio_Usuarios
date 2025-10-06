package org.example.storage;

import io.reactivex.rxjava3.core.Single;
import org.example.models.User;

import java.io.File;
import java.util.List;

public interface UserStorage {
    Single<Integer> fileWrite(File file, List<User> users);
}
