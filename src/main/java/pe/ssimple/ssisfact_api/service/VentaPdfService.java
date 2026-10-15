package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;

public interface VentaPdfService {
    byte[] generarPdf(VentaDetalleResponse detalle);
}
