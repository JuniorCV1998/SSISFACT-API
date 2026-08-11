package pe.ssimple.ssisfact_api.service.impl;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LocalSunatCacheTest {

    @Test
    void shouldReturnCachedValueBeforeExpiration() {
        LocalSunatCache cache = new LocalSunatCache(1800);
        cache.put("k", "v");
        assertEquals(Optional.of("v"), cache.get("k"));
    }

    @Test
    void shouldTreatZeroAsAValidEmptyResult() {
        LocalSunatCache cache = new LocalSunatCache(1800);
        cache.put("k", 0);
        Optional<Integer> cached = cache.get("k");
        assertTrue(cached.isPresent());
        assertEquals(0, cached.get());
    }

    @Test
    void shouldExpireAfterTtl() throws InterruptedException {
        LocalSunatCache cache = new LocalSunatCache(0);
        cache.put("k", "v");
        Thread.sleep(5);
        assertTrue(cache.get("k").isEmpty());
    }

    @Test
    void evictShouldRemoveEntryImmediately() {
        LocalSunatCache cache = new LocalSunatCache(1800);
        cache.put("k", "v");
        cache.evict("k");
        assertTrue(cache.get("k").isEmpty());
    }
}
