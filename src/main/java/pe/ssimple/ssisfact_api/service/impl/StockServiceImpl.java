package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.repository.StockRepository;
import pe.ssimple.ssisfact_api.service.StockService;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public StockResponse ingresarStock(StockRequest request) {

        // NORMALIZAR MOTIVO
        request.setMotivo(request.getMotivo().trim());

        return stockRepository.ingresarStock(request);
    }
}
