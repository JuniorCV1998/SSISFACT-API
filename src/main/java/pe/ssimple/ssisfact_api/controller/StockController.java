package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.service.StockService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/ingresar")
    public ResponseEntity<ApiResponse<StockResponse>> ingresarStock(
            @Valid @RequestBody StockRequest request) {

        return ResponseBuilder.build(stockService.ingresarStock(request), "OK");
    }
}
