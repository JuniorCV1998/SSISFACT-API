package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Cliente.ClienteListResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSimpleResponse;

import java.util.List;

public interface ClienteService {
    ClienteSaveResponse guardarCliente(ClienteRequest request);
    ClienteListResponse listarClientes(Long empresaId, String busqueda, int estado, int page, int size);
    List<ClienteSimpleResponse> listarClientesParaSeleccion(Long empresaId, String busqueda);
    ClienteSaveResponse desactivarCliente(Long clienteId, Long empresaId);
}
