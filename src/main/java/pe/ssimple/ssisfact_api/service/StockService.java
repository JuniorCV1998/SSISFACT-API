package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;

public interface StockService {
    StockResponse ingresarStock(StockRequest request);
}
