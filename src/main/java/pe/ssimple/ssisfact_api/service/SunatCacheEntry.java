package pe.ssimple.ssisfact_api.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatCacheEntry {
    private Object value;
    private Instant expiresAt;
}
