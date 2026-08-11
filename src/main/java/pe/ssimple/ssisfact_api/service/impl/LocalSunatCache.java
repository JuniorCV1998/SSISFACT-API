package pe.ssimple.ssisfact_api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.service.SunatCache;
import pe.ssimple.ssisfact_api.service.SunatCacheEntry;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache local en memoria (Caffeine-like, sin dependencias extra). Implementa
 * {@link SunatCache} para que migrar a Redis más adelante solo implique
 * cambiar esta clase por otra implementación del mismo contrato.
 */
@Service
public class LocalSunatCache implements SunatCache {

    private final Map<String, SunatCacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public LocalSunatCache(@Value("${sunat.cache.ttl-seconds:1800}") long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        SunatCacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(Instant.now())) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable((T) entry.getValue());
    }

    @Override
    public <T> void put(String key, T value) {
        cache.put(key, new SunatCacheEntry(value, Instant.now().plusSeconds(ttlSeconds)));
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }
}
