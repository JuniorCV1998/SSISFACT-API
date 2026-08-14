package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarResponse;

public interface StockService {
    StockResponse ingresarStock(StockRequest request);
    StockRetirarResponse retirarStock(StockRetirarRequest request);
}
