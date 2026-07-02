package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalItemResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalRequest;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalSaveResponse;

import java.util.List;

public interface SucursalRepository {
    SucursalSaveResponse guardarSucursal(SucursalRequest request);
    List<SucursalItemResponse> listarSucursales(Long empresaId, String busqueda, int estado, int page, int size);
}
