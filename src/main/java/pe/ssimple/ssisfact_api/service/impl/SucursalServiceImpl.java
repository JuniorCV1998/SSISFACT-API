package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalItemResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalListResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalRequest;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalSaveResponse;
import pe.ssimple.ssisfact_api.repository.SucursalRepository;
import pe.ssimple.ssisfact_api.service.SucursalService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;

    @Override
    public SucursalSaveResponse guardarSucursal(SucursalRequest request) {

        request.setNombre(request.getNombre().trim());
        request.setDireccion(request.getDireccion().trim());

        if (request.getTelefono() != null) {
            request.setTelefono(request.getTelefono().trim());
        }

        return sucursalRepository.guardarSucursal(request);
    }

    @Override
    public SucursalListResponse listarSucursales(Long empresaId, String busqueda, int estado, int page, int size) {

        List<SucursalItemResponse> items = sucursalRepository.listarSucursales(
                empresaId, busqueda.trim(), estado, page, size);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new SucursalListResponse(items, total, page, size);
    }
}
