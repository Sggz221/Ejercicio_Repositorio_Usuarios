package org.example.rest;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface UsersApiDao {
    /**
     * Solicitud GET a la API para recuperar todos los usuarios
     */
    @GET("users")
    Call<List<UserApiEntity>> getAll();

    /**
     * Solicitud GET a la API para obtener un usuario dado un ID
     * @param id
     */
    @GET("users/{id}")
    Call<UserApiEntity> getById(@Path("id") Long id);

    /**
     * Solicitud POST a la API para crear un usuario
     * @param user
     */
    @POST("users")
    Call<UserApiEntity> create(@Body UserApiEntity user);

    /**
     * Solicitud PUT a la API para actualizar el contenido de un usuario dado un ID
     * @param id
     * @param user
     */
    @PUT("users/{id}")
    Call<UserApiEntity> update(@Path("id") Long id, @Body UserApiEntity user);

    /**
     * Solicitud DELETE a la API para eliminar un usuario dado un ID
     * @param id
     * @return
     */
    @DELETE("users/{id}")
    Call<UserApiEntity> delete(@Path("id") Long id);
}
