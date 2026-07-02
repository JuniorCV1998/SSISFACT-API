package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalListResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalRequest;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalSaveResponse;

public interface SucursalService {
    SucursalSaveResponse guardarSucursal(SucursalRequest request);
    SucursalListResponse listarSucursales(Long empresaId, String busqueda, int estado, int page, int size);
}
