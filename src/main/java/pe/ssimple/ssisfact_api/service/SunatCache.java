package pe.ssimple.ssisfact_api.service;

import java.util.Optional;

public interface SunatCache {
    <T> Optional<T> get(String key);

    <T> void put(String key, T value);

    void evict(String key);
}
