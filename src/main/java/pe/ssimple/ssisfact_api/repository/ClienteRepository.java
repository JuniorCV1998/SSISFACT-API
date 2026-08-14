package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Cliente.ClienteItemResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;

import java.util.List;

public interface ClienteRepository {
    ClienteSaveResponse guardarCliente(ClienteRequest request);
    List<ClienteItemResponse> listarClientes(Long empresaId, String busqueda, int estado, int page, int size);
    ClienteSaveResponse desactivarCliente(Long clienteId, Long empresaId);
    Long obtenerClienteGenerico(Long empresaId);
}
