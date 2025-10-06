package org.example.dao;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public final class UserEntity {
    private final Long id;
    private final String name;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final LocalDateTime updatedAt = LocalDateTime.now();
}
