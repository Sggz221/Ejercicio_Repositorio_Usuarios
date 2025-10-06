package org.example.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.config.Config;
import org.example.models.User;

import java.util.concurrent.TimeUnit;

public class UserCache {
    public static synchronized Cache<Long, User> provideCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(Config.getInstance().getCacheSize())
                .build();
    }
}
