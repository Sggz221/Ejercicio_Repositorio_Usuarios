package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Clase que representa el modelo de un usuairo de la aplicacion.
 * Campos: {@code id}, {@code name}, {@code username}, {@code email}
 */
@Data
@AllArgsConstructor
public final class User {
    private final Long id;
    private final String name;
    private final String username;
    private final String email;
}
