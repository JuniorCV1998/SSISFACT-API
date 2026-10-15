package pe.ssimple.ssisfact_api.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Venta.PagoResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemDetalleResponse;
import pe.ssimple.ssisfact_api.exception.WhatsappIntegrationException;
import pe.ssimple.ssisfact_api.service.VentaPdfService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Comprobante de venta en PDF, rollo de 80 mm con alto variable. Fuente única para
 * imprimir y para enviar por WhatsApp. El color de marca de la empresa se usa solo
 * en la banda del título y en el total; el resto es gris neutro para que se vea serio.
 * No usa iconos (el facturador es multirubro). No vuelve a consultar la BD.
 */
@Slf4j
@Service
public class VentaPdfServiceImpl implements VentaPdfService {

    private static final float ANCHO_80MM = 226.77f;
    private static final float MARGEN = 12f;
    private static final float ALTO_MEDICION = 5000f;
    private static final float HOLGURA_ALTO = 40f;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color MARCA_POR_DEFECTO = new Color(0x1F2937);
    private static final Color TEXTO = new Color(0x111827);
    private static final Color GRIS = new Color(0x6B7280);
    private static final Color BORDE = new Color(0x9CA3AF);
    private static final float RADIO_BORDE = 6f;
    private static final float RADIO_BANDA = 8f;
    private static final Color FONDO_SUAVE = new Color(0xF9FAFB);
    private static final Color BLANCO = Color.WHITE;

    @Override
    public byte[] generarPdf(VentaDetalleResponse detalle) {
        try {
            float alto = medirAlto(detalle);
            return renderizar(detalle, alto);
        } catch (Exception e) {
            log.error("Error generando PDF de la venta {}: {}", detalle.getId(), e.getMessage());
            throw new WhatsappIntegrationException("No fue posible generar el comprobante en PDF.");
        }
    }

    private float medirAlto(VentaDetalleResponse detalle) throws DocumentException {
        Document documento = new Document(new Rectangle(ANCHO_80MM, ALTO_MEDICION), MARGEN, MARGEN, MARGEN, MARGEN);
        PdfWriter writer = PdfWriter.getInstance(documento, new ByteArrayOutputStream());
        documento.open();
        escribirContenido(documento, writer, detalle);
        float yFinal = writer.getVerticalPosition(true);
        documento.close();
        return (ALTO_MEDICION - yFinal) + MARGEN + HOLGURA_ALTO;
    }

    private byte[] renderizar(VentaDetalleResponse detalle, float alto) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document documento = new Document(new Rectangle(ANCHO_80MM, alto), MARGEN, MARGEN, MARGEN, MARGEN);
        PdfWriter writer = PdfWriter.getInstance(documento, out);
        documento.open();
        escribirContenido(documento, writer, detalle);
        documento.close();
        return out.toByteArray();
    }

    private void escribirContenido(Document documento, PdfWriter writer, VentaDetalleResponse d) throws DocumentException {
        Color marca = colorMarca(d.getEmpresaColorPrimario());
        Color textoSobreMarca = esClaro(marca) ? TEXTO : BLANCO;

        agregarEncabezado(documento, d, cargarLogo(d.getEmpresaLogoTicketUrl()));
        agregarBandaTitulo(documento, d, marca, textoSobreMarca);
        agregarCodigoBarras(documento, writer, d.getComprobanteCodigo());
        espacio(documento, 10);
        agregarTarjetaDatos(documento, d, marca);
        agregarItems(documento, d, marca, textoSobreMarca);
        agregarTotales(documento, d, marca);
        agregarPagos(documento, d, marca, textoSobreMarca);
        agregarSeparador(documento);
        agregarPie(documento, d);
    }

    private Image cargarLogo(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        byte[] bytes = descargarImagen(logoUrl);
        if (bytes == null) {
            return null;
        }
        try {
            Image imagen = Image.getInstance(bytes);
            imagen.scaleToFit(50f, 50f);
            return imagen;
        } catch (Exception e) {
            log.warn("No se pudo leer el logo del ticket: {}", e.getMessage());
            return null;
        }
    }

    private byte[] descargarImagen(String url) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (Exception e) {
            log.warn("No se pudo descargar el logo del ticket: {}", e.getMessage());
            return null;
        }
    }

    private void agregarEncabezado(Document documento, VentaDetalleResponse d, Image logo) throws DocumentException {
        PdfPCell info = new PdfPCell();
        info.setBorder(Rectangle.NO_BORDER);
        info.setVerticalAlignment(Element.ALIGN_MIDDLE);
        info.setPaddingLeft(10f);
        info.addElement(parrafo(Objects.toString(d.getEmpresaNombre(), ""), fuente(12, true, TEXTO), Element.ALIGN_LEFT));
        info.addElement(parrafo("RUC " + Objects.toString(d.getEmpresaRuc(), ""), fuente(8, false, GRIS), Element.ALIGN_LEFT));
        if (d.getEmpresaDireccion() != null) {
            info.addElement(parrafo(d.getEmpresaDireccion(), fuente(7.5f, false, GRIS), Element.ALIGN_LEFT));
        }
        if (d.getSucursalNombre() != null) {
            info.addElement(parrafo(d.getSucursalNombre(), fuente(7.5f, false, GRIS), Element.ALIGN_LEFT));
        }

        if (logo == null) {
            agregarCentrado(documento, d.getEmpresaNombre(), fuente(12, true, TEXTO));
            agregarCentrado(documento, "RUC " + Objects.toString(d.getEmpresaRuc(), ""), fuente(8, false, GRIS));
            if (d.getEmpresaDireccion() != null) {
                agregarCentrado(documento, d.getEmpresaDireccion(), fuente(7.5f, false, GRIS));
            }
            if (d.getSucursalNombre() != null) {
                agregarCentrado(documento, d.getSucursalNombre(), fuente(7.5f, false, GRIS));
            }
            espacio(documento, 10);
            return;
        }

        PdfPTable cabecera = new PdfPTable(new float[]{1f, 2.2f});
        cabecera.setWidthPercentage(100);

        PdfPCell celdaLogo = new PdfPCell(logo);
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setPaddingTop(4f);
        celdaLogo.setPaddingBottom(4f);
        celdaLogo.setPaddingLeft(0f);
        celdaLogo.setPaddingRight(8f);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaLogo.setHorizontalAlignment(Element.ALIGN_LEFT);
        cabecera.addCell(celdaLogo);
        cabecera.addCell(info);

        documento.add(cabecera);
        espacio(documento, 10);
    }

    private void agregarBandaTitulo(Document documento, VentaDetalleResponse d, Color marca, Color texto)
            throws DocumentException {
        PdfPTable banda = new PdfPTable(1);
        banda.setWidthPercentage(100);

        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setCellEvent(new FondoRedondeado(marca, RADIO_BANDA, false));
        celda.setPaddingTop(10f);
        celda.setPaddingBottom(10f);
        celda.setPaddingLeft(12f);
        celda.setPaddingRight(12f);
        celda.addElement(parrafo(Objects.toString(d.getDocumentoTitulo(), "COMPROBANTE").toUpperCase(),
                fuente(12, true, texto), Element.ALIGN_CENTER));
        if (d.getComprobanteCodigo() != null) {
            celda.addElement(parrafo("N° " + d.getComprobanteCodigo(), fuente(10, false, texto), Element.ALIGN_CENTER));
        }
        banda.addCell(celda);
        documento.add(banda);
        espacio(documento, 10);
    }

    private void agregarCodigoBarras(Document documento, PdfWriter writer, String codigo) throws DocumentException {
        if (codigo == null || codigo.isBlank()) {
            return;
        }
        Barcode128 barcode = new Barcode128();
        barcode.setCode(codigo);
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setBarHeight(26f);
        Image imagen = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
        imagen.setAlignment(Element.ALIGN_CENTER);
        documento.add(imagen);
    }

    private void agregarTarjetaDatos(Document documento, VentaDetalleResponse d, Color marca) throws DocumentException {
        PdfPTable tarjeta = new PdfPTable(new float[]{1.25f, 1f});
        tarjeta.setWidthPercentage(100);

        PdfPCell izquierda = new PdfPCell();
        izquierda.setBackgroundColor(FONDO_SUAVE);
        izquierda.setBorder(Rectangle.RIGHT);
        izquierda.setBorderColor(BORDE);
        izquierda.setBorderWidth(0.75f);
        izquierda.setPadding(8f);
        bloqueCampo(izquierda, "Cliente", d.getClienteNombre(), true);
        if (d.getClienteDocumento() != null) {
            bloqueCampo(izquierda, Objects.toString(d.getClienteTipoDocumento(), "Documento"), d.getClienteDocumento(), false);
        }
        if (d.getUsuarioNombre() != null) {
            bloqueCampo(izquierda, "Atendió", d.getUsuarioNombre(), false);
        }
        tarjeta.addCell(izquierda);

        PdfPCell derecha = new PdfPCell();
        derecha.setBackgroundColor(FONDO_SUAVE);
        derecha.setBorder(Rectangle.NO_BORDER);
        derecha.setPadding(8f);
        if (d.getFecha() != null) {
            bloqueCampo(derecha, "Fecha", d.getFecha().format(FORMATO_FECHA), false);
        }
        derecha.addElement(parrafo("ESTADO", fuente(6.5f, false, GRIS), Element.ALIGN_LEFT));
        derecha.addElement(pastilla(Objects.toString(d.getEstado(), ""), marca));
        tarjeta.addCell(derecha);

        documento.add(marcoRedondeado(tarjeta));
    }

    private void bloqueCampo(PdfPCell celda, String etiqueta, String valor, boolean destacado) {
        celda.addElement(parrafo(etiqueta.toUpperCase(), fuente(6.5f, false, GRIS), Element.ALIGN_LEFT));
        celda.addElement(parrafo(Objects.toString(valor, ""), fuente(destacado ? 8.5f : 8f, destacado, TEXTO),
                Element.ALIGN_LEFT));
        celda.addElement(new Paragraph(" ", fuente(4, false, FONDO_SUAVE)));
    }

    private Paragraph pastilla(String texto, Color marca) {
        Color fondo = mezclar(marca, BLANCO, 0.15f);
        Color textoPastilla = esClaro(marca) ? TEXTO : marca;
        Chunk chunk = new Chunk(" " + texto + " ", fuente(7.5f, true, textoPastilla));
        chunk.setBackground(fondo, 2f, 2f, 2f, 2f);
        Paragraph p = new Paragraph(chunk);
        p.setAlignment(Element.ALIGN_LEFT);
        return p;
    }

    private PdfPTable marcoRedondeado(PdfPTable interior) {
        PdfPTable marco = new PdfPTable(1);
        marco.setWidthPercentage(100);
        PdfPCell celda = new PdfPCell(interior);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(0f);
        celda.setCellEvent(new BordeRedondeado(BORDE, RADIO_BORDE));
        marco.addCell(celda);
        return marco;
    }

    private PdfPTable marcoConEncabezado(PdfPTable encabezado, PdfPTable cuerpo, Color fondo) {
        PdfPTable interior = new PdfPTable(1);
        interior.setWidthPercentage(100);

        PdfPCell celdaEncabezado = new PdfPCell(encabezado);
        celdaEncabezado.setBorder(Rectangle.NO_BORDER);
        celdaEncabezado.setPadding(0f);
        celdaEncabezado.setCellEvent(new FondoRedondeado(fondo, RADIO_BORDE, true));
        interior.addCell(celdaEncabezado);

        PdfPCell celdaCuerpo = new PdfPCell(cuerpo);
        celdaCuerpo.setBorder(Rectangle.NO_BORDER);
        celdaCuerpo.setPadding(0f);
        interior.addCell(celdaCuerpo);

        return marcoRedondeado(interior);
    }

    private static final class FondoRedondeado implements PdfPCellEvent {
        private final Color color;
        private final float radio;
        private final boolean cuadrarInferior;

        FondoRedondeado(Color color, float radio, boolean cuadrarInferior) {
            this.color = color;
            this.radio = radio;
            this.cuadrarInferior = cuadrarInferior;
        }

        @Override
        public void cellLayout(PdfPCell celda, Rectangle posicion, PdfContentByte[] lienzos) {
            PdfContentByte fondo = lienzos[PdfPTable.BACKGROUNDCANVAS];
            fondo.setColorFill(color);
            fondo.roundRectangle(posicion.getLeft(), posicion.getBottom(),
                    posicion.getWidth(), posicion.getHeight(), radio);
            fondo.fill();
            if (cuadrarInferior) {
                fondo.rectangle(posicion.getLeft(), posicion.getBottom(),
                        posicion.getWidth(), Math.min(radio, posicion.getHeight()));
                fondo.fill();
            }
        }
    }

    private static final class BordeRedondeado implements PdfPCellEvent {
        private final Color color;
        private final float radio;

        BordeRedondeado(Color color, float radio) {
            this.color = color;
            this.radio = radio;
        }

        @Override
        public void cellLayout(PdfPCell celda, Rectangle posicion, PdfContentByte[] lienzos) {
            PdfContentByte lineas = lienzos[PdfPTable.LINECANVAS];
            lineas.setColorStroke(color);
            lineas.setLineWidth(0.75f);
            lineas.roundRectangle(posicion.getLeft() + 0.4f, posicion.getBottom() + 0.4f,
                    posicion.getWidth() - 0.8f, posicion.getHeight() - 0.8f, radio);
            lineas.stroke();
        }
    }

    private Color mezclar(Color base, Color hacia, float proporcionBase) {
        int r = Math.round(base.getRed() * proporcionBase + hacia.getRed() * (1 - proporcionBase));
        int g = Math.round(base.getGreen() * proporcionBase + hacia.getGreen() * (1 - proporcionBase));
        int b = Math.round(base.getBlue() * proporcionBase + hacia.getBlue() * (1 - proporcionBase));
        return new Color(r, g, b);
    }

    private void agregarItems(Document documento, VentaDetalleResponse d, Color marca, Color textoSobreMarca)
            throws DocumentException {
        espacio(documento, 10);
        float[] anchos = {0.45f, 2.7f, 1.35f, 1.6f, 1.7f};
        PdfPTable encabezado = new PdfPTable(anchos);
        encabezado.setWidthPercentage(100);

        Font fuenteEncabezado = fuente(6.5f, true, textoSobreMarca);
        encabezado.addCell(celdaEncabezado("#", fuenteEncabezado, Element.ALIGN_LEFT));
        encabezado.addCell(celdaEncabezado("DESCRIPCIÓN", fuenteEncabezado, Element.ALIGN_LEFT));
        encabezado.addCell(celdaEncabezado("CANT.", fuenteEncabezado, Element.ALIGN_RIGHT));
        encabezado.addCell(celdaEncabezado("P. UNIT.", fuenteEncabezado, Element.ALIGN_RIGHT));
        encabezado.addCell(celdaEncabezado("IMPORTE", fuenteEncabezado, Element.ALIGN_RIGHT));

        PdfPTable cuerpo = new PdfPTable(anchos);
        cuerpo.setWidthPercentage(100);
        int n = 1;
        int total = d.getItems().size();
        for (VentaItemDetalleResponse item : d.getItems()) {
            boolean separador = n < total;
            cuerpo.addCell(celdaFila(String.valueOf(n), GRIS, Element.ALIGN_LEFT, false, separador));
            cuerpo.addCell(celdaFila(item.getProductoNombre(), TEXTO, Element.ALIGN_LEFT, false, separador));
            cuerpo.addCell(celdaFila(formato(item.getCantidad()), TEXTO, Element.ALIGN_RIGHT, false, separador));
            cuerpo.addCell(celdaFila(formato(item.getPrecioUnitario()), TEXTO, Element.ALIGN_RIGHT, false, separador));
            cuerpo.addCell(celdaFila(formato(item.getSubtotal()), TEXTO, Element.ALIGN_RIGHT, true, separador));
            n++;
        }
        documento.add(marcoConEncabezado(encabezado, cuerpo, marca));
    }

    private PdfPCell celdaEncabezado(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Paragraph(texto, fuente));
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPaddingTop(5f);
        celda.setPaddingBottom(5f);
        celda.setPaddingLeft(4f);
        celda.setPaddingRight(4f);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private PdfPCell celdaFila(String texto, Color color, int alineacion, boolean negrita, boolean separador) {
        PdfPCell celda = new PdfPCell(new Paragraph(Objects.toString(texto, ""), fuente(8, negrita, color)));
        celda.setBorder(separador ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        celda.setBorderColor(BORDE);
        celda.setBorderWidth(0.75f);
        celda.setPaddingTop(5f);
        celda.setPaddingBottom(5f);
        celda.setPaddingLeft(4f);
        celda.setPaddingRight(4f);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private void agregarTotales(Document documento, VentaDetalleResponse d, Color marca) throws DocumentException {
        espacio(documento, 10);
        PdfPTable bloque = new PdfPTable(new float[]{1.1f, 1f});
        bloque.setWidthPercentage(100);

        PdfPCell letras = new PdfPCell();
        letras.setBorder(Rectangle.NO_BORDER);
        letras.setVerticalAlignment(Element.ALIGN_MIDDLE);
        letras.setPaddingRight(8f);
        if (d.getMontoEnLetras() != null) {
            letras.addElement(parrafo(d.getMontoEnLetras(), fuente(7.5f, true, TEXTO), Element.ALIGN_LEFT));
        }
        bloque.addCell(letras);

        PdfPTable derecha = new PdfPTable(1);
        derecha.setWidthPercentage(100);

        PdfPCell subtotales = new PdfPCell();
        subtotales.setBorder(Rectangle.BOTTOM);
        subtotales.setBorderColor(BORDE);
        subtotales.setBorderWidth(0.75f);
        subtotales.setPadding(7f);
        subtotales.addElement(filaMonto("Subtotal", formato(d.getSubtotal()), 8, GRIS));
        if (d.getDescuento() != null && d.getDescuento().signum() > 0) {
            subtotales.addElement(filaMonto("Descuento", formato(d.getDescuento()), 8, GRIS));
        }
        if (d.getImpuestos() != null && d.getImpuestos().signum() > 0) {
            subtotales.addElement(filaMonto("IGV", formato(d.getImpuestos()), 8, GRIS));
        }
        derecha.addCell(subtotales);

        PdfPCell total = new PdfPCell();
        total.setBorder(Rectangle.NO_BORDER);
        total.setPadding(8f);
        total.addElement(filaMonto("TOTAL", formato(d.getTotal()), 14, marca));
        derecha.addCell(total);

        PdfPCell celdaDerecha = new PdfPCell(marcoRedondeado(derecha));
        celdaDerecha.setBorder(Rectangle.NO_BORDER);
        celdaDerecha.setPadding(0f);
        bloque.addCell(celdaDerecha);

        documento.add(bloque);
    }

    private Paragraph filaMonto(String etiqueta, String valor, float tamano, Color color) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + "   ", fuente(tamano - 1.5f, false, color)));
        p.add(new Chunk(valor, fuente(tamano, true, color)));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void agregarPagos(Document documento, VentaDetalleResponse d, Color marca, Color textoSobreMarca)
            throws DocumentException {
        if (d.getPagos() == null || d.getPagos().isEmpty()) {
            return;
        }
        espacio(documento, 10);
        float[] anchos = {3f, 2f};
        PdfPTable encabezado = new PdfPTable(1);
        encabezado.setWidthPercentage(100);
        PdfPCell celdaTitulo = new PdfPCell(new Paragraph("FORMA DE PAGO", fuente(6.5f, true, textoSobreMarca)));
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setPaddingTop(5f);
        celdaTitulo.setPaddingBottom(5f);
        celdaTitulo.setPaddingLeft(4f);
        encabezado.addCell(celdaTitulo);

        PdfPTable cuerpo = new PdfPTable(anchos);
        cuerpo.setWidthPercentage(100);
        BigDecimal vueltoTotal = BigDecimal.ZERO;
        for (PagoResponse pago : d.getPagos()) {
            if (pago.getVuelto() != null && pago.getVuelto().signum() > 0) {
                vueltoTotal = vueltoTotal.add(pago.getVuelto());
            }
        }
        boolean hayVuelto = vueltoTotal.signum() > 0;
        int pagosTotal = d.getPagos().size();
        int indice = 0;
        for (PagoResponse pago : d.getPagos()) {
            indice++;
            boolean separador = hayVuelto || indice < pagosTotal;
            cuerpo.addCell(celdaFila(pago.getMetodo(), TEXTO, Element.ALIGN_LEFT, false, separador));
            cuerpo.addCell(celdaFila(formato(pago.getMonto()), TEXTO, Element.ALIGN_RIGHT, true, separador));
        }
        if (hayVuelto) {
            cuerpo.addCell(celdaFila("Vuelto", GRIS, Element.ALIGN_LEFT, false, false));
            cuerpo.addCell(celdaFila(formato(vueltoTotal), GRIS, Element.ALIGN_RIGHT, false, false));
        }
        documento.add(marcoConEncabezado(encabezado, cuerpo, marca));
    }

    private void agregarPie(Document documento, VentaDetalleResponse d) throws DocumentException {
        if (d.getTicketMensajePie() == null || d.getTicketMensajePie().isBlank()) {
            return;
        }
        espacio(documento, 6);
        agregarCentrado(documento, d.getTicketMensajePie(), fuente(8.5f, false, GRIS));
    }

    private void agregarSeparador(Document documento) throws DocumentException {
        espacio(documento, 10);
        documento.add(new Paragraph(new Chunk(new LineSeparator(0.5f, 100, BORDE, Element.ALIGN_CENTER, 0))));
        espacio(documento, 4);
    }

    private void agregarCentrado(Document documento, String texto, Font fuente) throws DocumentException {
        documento.add(parrafo(texto == null ? "" : texto, fuente, Element.ALIGN_CENTER));
    }

    private void espacio(Document documento, float puntos) throws DocumentException {
        documento.add(new Paragraph(" ", fuente(puntos, false, BLANCO)));
    }

    private Paragraph parrafo(String texto, Font fuente, int alineacion) {
        Paragraph parrafo = new Paragraph(new Chunk(texto, fuente));
        parrafo.setAlignment(alineacion);
        return parrafo;
    }

    private Font fuente(float tamano, boolean negrita, Color color) {
        return FontFactory.getFont(negrita ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                tamano, Font.NORMAL, color);
    }

    private Color colorMarca(String hex) {
        if (hex != null && hex.matches("^#[0-9A-Fa-f]{6}$")) {
            return Color.decode(hex);
        }
        return MARCA_POR_DEFECTO;
    }

    private boolean esClaro(Color color) {
        return (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) > 160;
    }

    private String formato(BigDecimal valor) {
        return valor == null ? "" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
