package pe.ssimple.ssisfact_api.dto.Sunat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatPageResult<T> {
    private List<T> items;
    private long total;
}
