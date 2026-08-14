package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockRetirarResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.StockService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;
import pe.ssimple.ssisfact_api.util.SucursalAccessGuard;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final SucursalAccessGuard sucursalAccessGuard;

    @PostMapping("/ingresar")
    public ResponseEntity<ApiResponse<StockResponse>> ingresarStock(
            @Valid @RequestBody StockRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        sucursalAccessGuard.validar(user, request.getSucursalId());

        return ResponseBuilder.build(stockService.ingresarStock(request), "OK");
    }

    @PostMapping("/retirar")
    public ResponseEntity<ApiResponse<StockRetirarResponse>> retirarStock(
            @Valid @RequestBody StockRetirarRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        sucursalAccessGuard.validar(user, request.getSucursalId());

        return ResponseBuilder.build(stockService.retirarStock(request), "OK");
    }
}
