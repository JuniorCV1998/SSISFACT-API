package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.repository.StockRepository;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public StockResponse ingresarStock(StockRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_ingresar_stock(?,?,?,?,?)",
                (rs, rowNum) -> new StockResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getProductoId(),
                request.getSucursalId(),
                request.getCantidad(),
                request.getMotivo(),
                request.getCompraId());
    }
}
