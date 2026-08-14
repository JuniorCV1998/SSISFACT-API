package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarResponse;

public interface StockRepository {
    StockResponse ingresarStock(StockRequest request);
    StockRetirarResponse retirarStock(StockRetirarRequest request);
}
