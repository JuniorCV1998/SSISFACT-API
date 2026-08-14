package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarResponse;
import pe.ssimple.ssisfact_api.repository.StockRepository;
import pe.ssimple.ssisfact_api.service.StockService;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public StockResponse ingresarStock(StockRequest request) {

        // NORMALIZAR MOTIVO Y TIPO
        request.setMotivo(request.getMotivo().trim());
        request.setTipo(request.getTipo().trim().toUpperCase());

        return stockRepository.ingresarStock(request);
    }

    @Override
    public StockRetirarResponse retirarStock(StockRetirarRequest request) {

        // NORMALIZAR MOTIVO Y TIPO
        request.setMotivo(request.getMotivo().trim());
        request.setTipo(request.getTipo().trim().toUpperCase());

        return stockRepository.retirarStock(request);
    }
}
