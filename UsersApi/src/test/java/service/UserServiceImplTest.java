package service;

import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.example.exceptions.UserException;
import org.example.models.User;
import org.example.repository.UserRemoteRepositoryImpl;
import org.example.repository.UserRepositoryImpl;
import org.example.service.UserServiceImpl;
import org.example.storage.UserStorageImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private UserRepositoryImpl localRepository;
    private UserRemoteRepositoryImpl remoteRepository;
    private UserStorageImpl storage;
    private Cache<Long, User> cache;

    private UserServiceImpl service;
    @BeforeEach
    void setUp() {
        // Esta linea sirve para poder redefinir o cambiar el Scheduler de las funciones que se testean para poder ejecutar los tests de funciones asincronas
        // sin que los hilos afecten al orden y tiempos de ejecucion de tests
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        localRepository = mock(UserRepositoryImpl.class);
        remoteRepository = mock(UserRemoteRepositoryImpl.class);
        storage = mock(UserStorageImpl.class);
        cache = mock(Cache.class);

        service = new UserServiceImpl(localRepository, remoteRepository, storage, cache);
    }

    @AfterEach
    void tearDown() {
        RxJavaPlugins.reset(); //Para restaurar el comportamiemto original del Scheduler despues de redefinirlo en el setup
    }

    private final User user = new User(1L, "Pepe", "pepelocatis", "pepelocatis@mail.com");


    @Test
    void getAllLocal() {
        when(localRepository.getAll()).thenReturn(Single.just(List.of(user)));

        var result = service.getAll().test();

        result.assertComplete()
                .assertNoErrors()
                .assertValue(users -> users.size() == 1 && users.get(0).equals(user));

        verify(localRepository).getAll();
        verify(remoteRepository, never()).getAll();
    }

    @Test
    void getAllLocalEmptyRemoteHasData() {
        when(localRepository.getAll()).thenReturn(Single.just(List.of()));
        when(remoteRepository.getAll()).thenReturn(Single.just(List.of(user)));

        var test = service.getAll().test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(users -> users.size() == 1 && users.get(0).equals(user));

        verify(remoteRepository).getAll();
        verify(localRepository).save(user);
    }

    @Test
    void getByIdFromCache() {
        when(cache.getIfPresent(1L)).thenReturn(user);

        var test = service.getById(1L).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verifyNoInteractions(localRepository, remoteRepository);
    }

    @Test
    void getByIdFromLocal() {
        when(cache.getIfPresent(1L)).thenReturn(null);
        when(localRepository.getById(1L)).thenReturn(Maybe.just(user));
        when(remoteRepository.getById(anyLong())).thenReturn(Maybe.empty()); // necesario para evitar NPE

        var test = service.getById(1L).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verify(cache).put(1L, user);
        verify(localRepository).getById(1L);

        // no uses never() con remoteRepository, usa:
        verify(remoteRepository).getById(anyLong()); // o verifica número exacto de llamadas si quieres
    }


    @Test
    void getByIdFromRemoteWhenLocalEmpty() {
        when(cache.getIfPresent(1L)).thenReturn(null);
        when(localRepository.getById(1L)).thenReturn(Maybe.empty());
        when(remoteRepository.getById(1L)).thenReturn(Maybe.just(user));

        var test = service.getById(1L).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verify(cache).put(1L, user);
        verify(localRepository).save(user);
    }

    @Test
    void getByIdNotFoundAnywhere() {
        when(cache.getIfPresent(1L)).thenReturn(null);
        when(localRepository.getById(1L)).thenReturn(Maybe.empty());
        when(remoteRepository.getById(1L)).thenReturn(Maybe.empty());

        var test = service.getById(1L).test();

        test.assertError(UserException.class);
    }

    @Test
    void saveOk() {
        when(remoteRepository.save(user)).thenReturn(Maybe.just(user));

        var test = service.save(user).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verify(cache).put(1L, user);
        verify(localRepository).save(user);
    }

    @Test
    void saveFailsValidation() {
        User invalid = new User(0L, null, "", "");

        var test = service.save(invalid).test();

        test.assertError(Exception.class);
        verifyNoInteractions(remoteRepository);
    }

    @Test
    void updateOk() {
        when(remoteRepository.update(eq(user), eq(1L))).thenReturn(Maybe.just(user));

        var test = service.update(user, 1L).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verify(cache).put(1L, user);
        verify(localRepository).update(user, 1L);
    }

    @Test
    void updateNotFound() {
        when(remoteRepository.update(eq(user), eq(1L))).thenReturn(Maybe.empty());

        var test = service.update(user, 1L).test();

        test.assertError(UserException.class);
    }


    @Test
    void deleteOk() {
        when(remoteRepository.delete(1L)).thenReturn(Maybe.just(user));

        var test = service.delete(1L).test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(u -> u.equals(user));

        verify(localRepository).delete(1L);
        verify(cache).invalidate(1L);
    }

    @Test
    void deleteNotFound() {
        when(remoteRepository.delete(1L)).thenReturn(Maybe.empty());

        var test = service.delete(1L).test();

        test.assertError(UserException.class);
    }


    @Test
    void fileWriteOk() {
        File file = mock(File.class);
        when(storage.fileWrite(file, List.of(user))).thenReturn(Single.just(1));

        var test = service.fileWrite(file, List.of(user)).test();

        test.assertComplete()
                .assertValue(1);
        verify(storage).fileWrite(file, List.of(user));
    }

    @Test
    void fileWriteError() {
        File file = mock(File.class);
        when(storage.fileWrite(file, List.of(user))).thenReturn(Single.error(new RuntimeException("Error")));

        var test = service.fileWrite(file, List.of(user)).test();

        test.assertError(RuntimeException.class);
    }


    @Test
    void refreshLocalRepositoryOk() {
        when(remoteRepository.getAll()).thenReturn(Single.just(List.of(user)));

        var test = service.refreshLocalRepository().test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(true);

        verify(localRepository).deleteAll();
        verify(localRepository).save(user);
    }

    @Test
    void refreshLocalRepositoryError() {
        when(remoteRepository.getAll()).thenReturn(Single.error(new RuntimeException("Error")));

        var test = service.refreshLocalRepository().test();

        test.assertComplete()
                .assertNoErrors()
                .assertValue(false);

        verify(localRepository).deleteAll();
    }
}
