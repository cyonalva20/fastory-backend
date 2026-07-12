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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reportes")
@lombok.RequiredArgsConstructor
public class ReporteController {

    private static final String NO_PRODUCTS_MESSAGE = "No se encontraron productos en el inventario";

    private final MovimientoService movimientoService;

    @Autowired // Inyección del nuevo servicio
    private ReporteExportService reporteExportService;

    private final DetalleMovimientoRepository detalleRepository; // Inyectamos repositorio directamente para reportes de
                                                                 // lectura

    /**
     * Endpoint 1: Genera el reporte de stock actual en formato JSON (para la
     * tabla).
     * URL: GET /api/v1/reportes/stock-actual
     */
    @GetMapping("/stock-actual")
    public ResponseEntity<Object> generarReporteStockActual(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Boolean stockBajoMinimo,
            @RequestParam(defaultValue = "nombreProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            List<ReporteDto> reporte = movimientoService.generarReporteStockActual(
                    categoriaId,
                    marca,
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

    // -------------------------------------------------------------------------
    // --- NUEVO ENDPOINT PARA EXPORTACIÓN (PDF y Excel) ---
    // -------------------------------------------------------------------------

    /**
     * Endpoint 2: Exporta el reporte de stock actual a PDF o Excel.
     * URL: GET /api/v1/reportes/stock-actual/export?formato=excel
     */
    @GetMapping("/stock-actual/export")
    public ResponseEntity<Object> exportarReporteStockActual(
            @RequestParam(required = true) String formato, // Parámetro obligatorio: excel o pdf
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Boolean stockBajoMinimo,
            @RequestParam(defaultValue = "nombreProducto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            // 1. Obtener la data filtrada (reutilizamos el metodo del servicio)
            List<ReporteDto> data = movimientoService.generarReporteStockActual(
                    categoriaId, marca, stockBajoMinimo, sortBy, sortDir);

            // Criterio de Aceptación: Mensaje si no hay productos
            if (data.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", NO_PRODUCTS_MESSAGE));
            }

            ByteArrayOutputStream outputStream;
            String filename;
            MediaType mediaType;

            // 2. Generar el archivo según el formato solicitado
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

            // 3. Configurar las Cabeceras HTTP para forzar la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment").filename(filename).build());

            // 4. Devolver la respuesta binaria
            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al exportar el reporte: " + e.getMessage()));
        }
    }

    // --- NUEVOS ENDPOINTS ANALÍTICOS ---

    @GetMapping("/mas-vendidos")
    public ResponseEntity<Object> reporteMasVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<ReportesAnaliticosDto.ProductoMasVendidoProjection> data = detalleRepository
                .findProductosMasVendidos(inicio, fin);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/baja-rotacion")
    public ResponseEntity<Object> reporteBajaRotacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam Long umbral) {

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<ReportesAnaliticosDto.ProductoBajaRotacionProjection> data = detalleRepository
                .findProductosBajaRotacion(inicio, fin, umbral);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/entradas-proveedor")
    public ResponseEntity<Object> reporteEntradasProveedor(
            @RequestParam Integer idProveedor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<ReportesAnaliticosDto.EntradaProveedorProjection> data = detalleRepository
                .findEntradasPorProveedor(idProveedor, inicio, fin);

        return ResponseEntity.ok(data);
    }
}