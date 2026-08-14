package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoItemResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoRequest;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.repository.ProductoRepository;
import pe.ssimple.ssisfact_api.service.ProductoService;
import pe.ssimple.ssisfact_api.service.StockService;
import pe.ssimple.ssisfact_api.util.SqlErrorMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public ProductoResponse guardarProducto(ProductoRequest request) {

        // VALIDAR NOMBRE
        if (request.getNombre().trim().isEmpty()) {
            return new ProductoResponse(
                    "ERROR_NOMBRE",
                    "El nombre del producto no puede estar vacío",
                    0L);
        }

        // VALIDAR STOCK INICIAL (si se especifica sucursal, cantidad y motivo son obligatorios)
        if (request.getSucursalId() != null) {
            if (request.getCantidadInicial() == null || request.getCantidadInicial() <= 0) {
                return new ProductoResponse("ERROR_STOCK", "La cantidad inicial debe ser mayor a 0", 0L);
            }
            if (request.getMotivoIngreso() == null || request.getMotivoIngreso().trim().isEmpty()) {
                return new ProductoResponse("ERROR_STOCK", "El motivo del ingreso de stock es obligatorio", 0L);
            }
            request.setMotivoIngreso(request.getMotivoIngreso().trim());
        }

        // NORMALIZAR STRINGS
        request.setNombre(request.getNombre().trim());
        request.setCodigo(request.getCodigo().trim());
        request.setCategoriaNombre(request.getCategoriaNombre().trim());

        if (request.getCodigoBarras() != null) {
            request.setCodigoBarras(request.getCodigoBarras().trim());
        }

        ProductoResponse response = productoRepository.guardarProducto(request);

        if ("ERROR_SQL".equals(response.getEstado())) {
            int code = SqlErrorMapper.extractCode(response.getMensaje());
            log.error("[guardarProducto] Error SQL en SP — empresaId={} codigo='{}' sqlCode={} detalle='{}'",
                    request.getEmpresaId(), request.getCodigo(), code, response.getMensaje());
            return new ProductoResponse("ERROR_SQL", SqlErrorMapper.toSafeMessage(response.getMensaje()), 0L);
        }

        if (!"OK".equals(response.getEstado())) {
            log.warn("[guardarProducto] SP devolvió error — estado={} mensaje='{}' empresaId={} codigo={}",
                    response.getEstado(), response.getMensaje(),
                    request.getEmpresaId(), request.getCodigo());
            return response;
        }

        // INGRESAR STOCK INICIAL (opcional)
        if (request.getSucursalId() != null) {

            StockRequest stockRequest = new StockRequest();
            stockRequest.setProductoId(response.getId());
            stockRequest.setSucursalId(request.getSucursalId());
            stockRequest.setCantidad(request.getCantidadInicial());
            stockRequest.setTipo(request.getCompraId() != null ? "COMPRA" : "AJUSTE");
            stockRequest.setMotivo(request.getMotivoIngreso());
            stockRequest.setCompraId(request.getCompraId());

            StockResponse stockResponse = stockService.ingresarStock(stockRequest);

            if (!"OK".equals(stockResponse.getEstado())) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

                String mensaje = "ERROR_SQL".equals(stockResponse.getEstado())
                        ? SqlErrorMapper.toSafeMessage(stockResponse.getMensaje())
                        : stockResponse.getMensaje();

                log.error("[guardarProducto] Error al ingresar stock inicial — productoId={} sucursalId={} estado={} mensaje='{}'",
                        response.getId(), request.getSucursalId(), stockResponse.getEstado(), stockResponse.getMensaje());

                return new ProductoResponse("ERROR_STOCK", mensaje, 0L);
            }

            response.setStock(stockResponse);
        }

        return response;
    }

    @Override
    public ProductoListResponse listarProductos(Long empresaId, String busqueda, int page, int size, int estado, int mostrarCosto, Long sucursalId) {

        List<ProductoItemResponse> items = productoRepository.listarProductos(
                empresaId, busqueda.trim(), page, size, estado, mostrarCosto, sucursalId);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new ProductoListResponse(items, total, page, size);
    }

    @Override
    public ProductoCatalogoListResponse listarCatalogo(Long empresaId, String busqueda, int page, int size, Long sucursalId) {

        List<ProductoCatalogoResponse> items = productoRepository.listarCatalogo(
                empresaId, busqueda.trim(), page, size, sucursalId);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new ProductoCatalogoListResponse(items, total, page, size);
    }

    @Override
    public ProductoResponse activarProducto(Long productoId, Long empresaId) {
        return productoRepository.activarProducto(productoId, empresaId);
    }

    @Override
    public ProductoResponse desactivarProducto(Long productoId, Long empresaId) {
        return productoRepository.desactivarProducto(productoId, empresaId);
    }

    @Override
    public ProductoResponse eliminarProducto(Long productoId, Long empresaId) {
        return productoRepository.eliminarProducto(productoId, empresaId);
    }
}
