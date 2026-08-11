package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Sunat.SunatMessageDto;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatPageResult;

public interface SunatMensajeRepository {
    void upsert(Long empresaId, String ruc, SunatMessageDto mensaje);
    SunatPageResult<SunatMessageDto> listar(Long empresaId, int pagina, int tamanio);
}
