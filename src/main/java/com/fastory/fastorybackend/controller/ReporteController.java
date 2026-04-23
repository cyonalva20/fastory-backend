package com.fastory.fastorybackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fastory.fastorybackend.dto.ReporteDto;
import com.fastory.fastorybackend.dto.ReportesAnaliticosDto;
import com.fastory.fastorybackend.repository.DetalleMovimientoRepository;
import com.fastory.fastorybackend.service.MovimientoService;
import com.fastory.fastorybackend.service.ReporteExportService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reportes")
@lombok.RequiredArgsConstructor
public class ReporteController {

    private static final String NO_PRODUCTS_MESSAGE = "No se encontraron productos en el inventario";

    private final MovimientoService movimientoService;

    @Autowired
    private ReporteExportService reporteExportService;

    private final DetalleMovimientoRepository detalleRepository;

    /**
     * Endpoint 1: Genera el reporte de stock actual en formato JSON (para la
     * tabla).
     */
    @GetMapping("/stock-actual")
    public ResponseEntity<Object> generarReporteStockActual(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean stockBajoMinimo,
            @RequestParam(defaultValue = "nombreProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            List<ReporteDto> reporte = movimientoService.generarReporteStockActual(
                    categoriaId,
                    stockBajoMinimo,
                    sortBy,
                    sortDir);

            if (reporte.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", NO_PRODUCTS_MESSAGE));
            }

            return ResponseEntity.ok(reporte);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al generar el reporte: " + e.getMessage()));
        }
    }

    /**
     * Endpoint 2: Exporta el reporte de stock actual a PDF o Excel.
     */
    @GetMapping("/stock-actual/export")
    public ResponseEntity<Object> exportarReporteStockActual(
            @RequestParam(required = true) String formato,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean stockBajoMinimo,
            @RequestParam(defaultValue = "nombreProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            List<ReporteDto> data = movimientoService.generarReporteStockActual(
                    categoriaId, stockBajoMinimo, sortBy, sortDir);

            if (data.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", NO_PRODUCTS_MESSAGE));
            }

            ByteArrayOutputStream outputStream;
            String filename;
            MediaType mediaType;

            if (formato.equalsIgnoreCase("excel")) {
                outputStream = reporteExportService.exportToExcel(data);
                filename = "reporte_stock_actual.xlsx";
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else if (formato.equalsIgnoreCase("pdf")) {
                outputStream = reporteExportService.exportToPdf(data);
                filename = "reporte_stock_actual.pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formato de exportación no válido. Use 'excel' o 'pdf'."));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment").filename(filename).build());

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al exportar el reporte: " + e.getMessage()));
        }
    }

    // --- ENDPOINTS ANALÍTICOS ---

    @GetMapping("/mas-vendidos")
    public ResponseEntity<Object> reporteMasVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        OffsetDateTime inicio = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime fin = hasta.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<ReportesAnaliticosDto.ProductoMasVendidoProjection> data = detalleRepository
                .findProductosMasVendidos(inicio, fin);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/baja-rotacion")
    public ResponseEntity<Object> reporteBajaRotacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam Long umbral) {

        OffsetDateTime inicio = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime fin = hasta.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<ReportesAnaliticosDto.ProductoBajaRotacionProjection> data = detalleRepository
                .findProductosBajaRotacion(inicio, fin, umbral);

        return ResponseEntity.ok(data);
    }

}