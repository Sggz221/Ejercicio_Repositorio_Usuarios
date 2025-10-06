package org.example.rest;

import org.example.config.Config;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Clase que sirve de cliente para realizar peticiones HTTP a un servicio REST.
 */
// Clase que hace las peticiones a la API con métodos HTTP.
public class RetrofitClient {
    private static Retrofit retrofit = null;

    public synchronized static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.getInstance().getApiUrl())
                    .addConverterFactory(JacksonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
