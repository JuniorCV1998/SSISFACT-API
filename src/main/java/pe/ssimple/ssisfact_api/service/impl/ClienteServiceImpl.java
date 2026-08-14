package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteItemResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteListResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSimpleResponse;
import pe.ssimple.ssisfact_api.repository.ClienteRepository;
import pe.ssimple.ssisfact_api.service.ClienteService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteSaveResponse guardarCliente(ClienteRequest request) {

        if (request.getNombre().trim().isEmpty()) {
            return new ClienteSaveResponse("ERROR_NOMBRE", "El nombre del cliente no puede estar vacío", 0L);
        }

        request.setTipoDocumento(request.getTipoDocumento().trim().toUpperCase());
        request.setNumeroDocumento(request.getNumeroDocumento().trim());
        request.setNombre(request.getNombre().trim());

        if (request.getTelefono() != null) request.setTelefono(request.getTelefono().trim());
        if (request.getDireccion() != null) request.setDireccion(request.getDireccion().trim());

        return clienteRepository.guardarCliente(request);
    }

    @Override
    public ClienteListResponse listarClientes(Long empresaId, String busqueda, int estado, int page, int size) {

        List<ClienteItemResponse> items = clienteRepository.listarClientes(
                empresaId, busqueda.trim(), estado, page, size);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new ClienteListResponse(items, total, page, size);
    }

    @Override
    public List<ClienteSimpleResponse> listarClientesParaSeleccion(Long empresaId, String busqueda) {
        return clienteRepository.listarClientes(empresaId, busqueda.trim(), 1, 1, 50).stream()
                .map(cliente -> new ClienteSimpleResponse(
                        cliente.getId(), cliente.getNombre(), cliente.getNumeroDocumento()))
                .toList();
    }

    @Override
    public ClienteSaveResponse desactivarCliente(Long clienteId, Long empresaId) {
        return clienteRepository.desactivarCliente(clienteId, empresaId);
    }
}
