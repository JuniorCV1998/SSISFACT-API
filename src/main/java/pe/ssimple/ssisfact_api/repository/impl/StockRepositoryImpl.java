package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarResponse;
import pe.ssimple.ssisfact_api.repository.StockRepository;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public StockResponse ingresarStock(StockRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_ingresar_stock(?,?,?,?,?,?)",
                (rs, rowNum) -> new StockResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getProductoId(),
                request.getSucursalId(),
                request.getCantidad(),
                request.getTipo(),
                request.getMotivo(),
                request.getCompraId());
    }

    @Override
    public StockRetirarResponse retirarStock(StockRetirarRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_retirar_stock(?,?,?,?,?,?)",
                (rs, rowNum) -> new StockRetirarResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getInt("stock")),
                request.getProductoId(),
                request.getSucursalId(),
                request.getCantidad(),
                request.getTipo(),
                request.getMotivo(),
                request.getReferenciaId());
    }
}
