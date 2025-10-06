package repository;

import io.reactivex.rxjava3.observers.TestObserver;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.example.exceptions.UserException;
import org.example.mapper.UserMapper;
import org.example.models.User;
import org.example.repository.UserRemoteRepositoryImpl;
import org.example.rest.UserApiEntity;
import org.example.rest.UsersApiDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

class UserRemoteRepositoryImplTest {

    private UsersApiDao api;
    private UserRemoteRepositoryImpl repository;
    private UserApiEntity userApiEntity;
    private User user;

    @BeforeEach
    void setUp() {
        api = mock(UsersApiDao.class);
        repository = new UserRemoteRepositoryImpl(api);

        userApiEntity = new UserApiEntity(1L, "Pepe", "pepelocatis", "pepelocatis@gmail.com");
        user = UserMapper.toUser(userApiEntity);
    }


    @Test
    void getAllOk() throws IOException {
        // Hay que mockear tanto la llamada a la API como su respuesta
        Call<List<UserApiEntity>> call = mock(Call.class);
        Response<List<UserApiEntity>> response = Response.success(List.of(userApiEntity));

        when(api.getAll()).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<List<User>> testObserver = repository.getAll().test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(users -> users.size() == 1 && users.get(0).getId() == 1L);

        verify(api, times(1)).getAll();
    }


    @Test
    void getAllApiError() throws IOException {
        Call<List<UserApiEntity>> call = mock(Call.class);
        Response<List<UserApiEntity>> response = Response.error(500, ResponseBody.create(MediaType.parse("users"), ""));

        when(api.getAll()).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<List<User>> testObserver = repository.getAll().test();

        testObserver.assertError(UserException.ApiException.class);

        verify(api, times(1)).getAll();
    }


    @Test
    void getByIdOk() throws IOException {
        Call<UserApiEntity> call = mock(Call.class);
        Response<UserApiEntity> response = Response.success(userApiEntity);

        when(api.getById(1L)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<User> testObserver = repository.getById(1L).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> u.getId() == 1L);

        verify(api, times(1)).getById(1L);
    }


    @Test
    void getByIdNotFound() throws IOException {
        Call<UserApiEntity> call = mock(Call.class);
        Response<UserApiEntity> response = Response.error(500, ResponseBody.create(MediaType.parse("users/99"), ""));

        when(api.getById(99L)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<User> testObserver = repository.getById(99L).test();

        // el repositorio devuelve Maybe.empty(), por lo que completa sin error ni value
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();

        verify(api, times(1)).getById(99L);
    }


    @Test
    void saveOk() throws IOException {
        Call<UserApiEntity> call = mock(Call.class);
        Response<UserApiEntity> response = Response.success(userApiEntity);

        when(api.create(any(UserApiEntity.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<User> testObserver = repository.save(user).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> u.getId() == 1L);

        verify(api, times(1)).create(any(UserApiEntity.class));
    }


    @Test
    void saveError() throws IOException {
        Call<UserApiEntity> call = mock(Call.class);
        Response<UserApiEntity> response = Response.error(500, ResponseBody.create(MediaType.parse("users"), ""));

        when(api.create(any(UserApiEntity.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        TestObserver<User> testObserver = repository.save(user).test();

        testObserver.assertError(UserException.ApiException.class);

        verify(api, times(1)).create(any(UserApiEntity.class));
    }
    @Test
    void deleteOk() throws IOException {
        // Arrange
        Call<UserApiEntity> getCall = (Call<UserApiEntity>) mock(Call.class);
        Call<UserApiEntity> deleteCall = (Call<UserApiEntity>) mock(Call.class);

        Response<UserApiEntity> getResponse = Response.success(userApiEntity);
        Response<UserApiEntity> deleteResponse = Response.success(userApiEntity);

        when(api.getById(1L)).thenReturn(getCall);
        when(getCall.execute()).thenReturn(getResponse);
        when(api.delete(1L)).thenReturn(deleteCall);
        when(deleteCall.execute()).thenReturn(deleteResponse);

        // Act
        var testObserver = repository.delete(1L).test();

        // Assert
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> u.getId().equals(1L));

        // Verify
        verify(api, times(1)).getById(1L);
        verify(api, times(1)).delete(1L);
    }

    @Test
    void deleteNotFound() throws IOException {
        Call<UserApiEntity> getCall = (Call<UserApiEntity>) mock(Call.class);
        Response<UserApiEntity> response = Response.error(500, ResponseBody.create(MediaType.parse("users/99"), ""));

        when(api.getById(99L)).thenReturn(getCall);
        when(getCall.execute()).thenReturn(response);

        // Act
        var testObserver = repository.delete(99L).test();

        // Assert: el repo atrapa el error y retorna Maybe.empty()
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();

        // Verify
        verify(api, times(1)).getById(99L);
        verify(api, never()).delete(anyLong());
    }

    @Test
    void updateOk() throws IOException {
        // Arrange
        Call<UserApiEntity> getCall = (Call<UserApiEntity>) mock(Call.class);
        Call<UserApiEntity> updateCall = (Call<UserApiEntity>) mock(Call.class);

        Response<UserApiEntity> getResponse = Response.success(userApiEntity);
        Response<UserApiEntity> updateResponse = Response.success(userApiEntity);

        when(api.getById(1L)).thenReturn(getCall);
        when(getCall.execute()).thenReturn(getResponse);
        when(api.update(eq(1L), any(UserApiEntity.class))).thenReturn(updateCall);
        when(updateCall.execute()).thenReturn(updateResponse);

        // Act
        var testObserver = repository.update(user, 1L).test();

        // Assert
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> u.getId().equals(1L));

        // Verify
        verify(api, times(1)).getById(1L);
        verify(api, times(1)).update(eq(1L), any(UserApiEntity.class));
    }

    @Test
    void updateNotFound() throws IOException {
        // Arrange
        Call<UserApiEntity> call = (Call<UserApiEntity>) mock(Call.class);
        Response<UserApiEntity> response = Response.error(500, ResponseBody.create(MediaType.parse("users/99"), ""));

        when(api.getById(99L)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // Act
        var testObserver = repository.update(user, ((99L))).test();

        // Assert
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();

        // Verify
        verify(api, times(1)).getById(99L);
        verify(api, never()).update(anyLong(), any());
    }
}
