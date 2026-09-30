package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDiaResponse {
    // Abreviatura en español: LUN, MAR, MIE, JUE, VIE, SAB, DOM
    private String dia;
    private LocalDate fecha;
    private BigDecimal total;
}
