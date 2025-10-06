package org.example;

import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import org.example.cache.UserCache;
import org.example.dao.UserDao;
import org.example.database.JdbiManager;
import org.example.models.User;
import org.example.repository.UserRemoteRepositoryImpl;
import org.example.repository.UserRepositoryImpl;
import org.example.rest.RetrofitClient;
import org.example.rest.UsersApiDao;
import org.example.service.UserServiceImpl;
import org.example.storage.UserStorageImpl;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Dependencias
        UserDao dao = JdbiManager.getInstance().getUsersDao();
        UsersApiDao rest = RetrofitClient.getClient().create(UsersApiDao.class);
        UserRepositoryImpl localRepository = new UserRepositoryImpl(dao);
        UserRemoteRepositoryImpl remoteRepository = new UserRemoteRepositoryImpl(rest);
        UserStorageImpl storage = new UserStorageImpl();
        Cache<Long, User> cache = UserCache.provideCache();

        // Servicio
        UserServiceImpl service = new UserServiceImpl(localRepository, remoteRepository, storage, cache);

        User prueba = new User(1L, "Pepe", "pepelocatis", "pepelocatis@gmail.com");

        System.out.println("Todas las operaciones CRUD de manera asincrona\n");

        // --- Parte 1: ejecutar todas las operaciones CRUD para probar ---
        Maybe<List<User>> getAll = service.getAll().toMaybe();
        Maybe<User> getById = service.getById(1);
        Maybe<User> create = service.save(prueba);
        Maybe<User> delete = service.delete(1);
        Maybe<User> update = service.update(prueba, 1);

        Maybe.zip(
                getById,
                getAll,
                create,
                update,
                delete,
                (userId, users, created, updated, deleted) -> {
                    System.out.println("\n-> Realizando llamadas...");
                    System.out.println("\nGetAll: ");
                    users.forEach(System.out::println);
                    System.out.println("\nGetById: " + userId);
                    System.out.println("\nCreated: " + created);
                    System.out.println("\nUpdated: " + updated);
                    System.out.println("\nDeleted: " + deleted);
                    return "Todas las llamadas completadas con exito";
                }
        ).blockingSubscribe(
                result -> System.out.println("\n" + result),
                error -> System.out.println("Error: " + error.getMessage())
        );

        // Sincronizar el repositorio remoto con el local. He puesto 5 segundos para probar y no tener que esperar los 30 que pide el ejercicio
        Observable.interval(0, 5, TimeUnit.SECONDS)
                // Mapeamos la funcion con sinngle porque la funcion de refresco devuelve un Single<boolean>
                .flatMapSingle(evento -> // evento se refiere a un numero que va emitiendo rxjava de fondo a modo de "notificacion" para saber cuando ejecutar la lambda
                        service.refreshLocalRepository()
                                .doOnSuccess(success -> {
                                    if (success)
                                        System.out.println("Refresh completado OK");
                                    else
                                        System.out.println("Refresh fallido");
                                })
                                .doOnError(err -> System.err.println("Error al sincronizar: " + err.getMessage()))
                                .onErrorReturnItem(false)
                )
                .subscribe();

        // Mantener la aplicación viva indefinidamente
        Thread.currentThread().join();
        // Se podria parar cuando uno quiera forzando la salida con System.exit(0)
    }
}
