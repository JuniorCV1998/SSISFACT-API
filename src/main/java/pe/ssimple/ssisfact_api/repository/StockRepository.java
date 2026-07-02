package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;

public interface StockRepository {
    StockResponse ingresarStock(StockRequest request);
}
