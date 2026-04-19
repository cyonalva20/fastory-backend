package com.fastory.fastorybackend.service;

import com.fastory.fastorybackend.dto.ReporteDto;
// Importaciones para PDF (OpenPDF / iText)
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
// Importaciones para Excel (Apache POI)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteExportService {

    private final String[] HEADERS = {
            "ID", "Nombre", "Categoría", "Ubicación",
            "Stock Disp.", "Stock Mínimo"
    };

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Genera un archivo Excel (XLSX) con la fecha de generación.
     */
    public ByteArrayOutputStream exportToExcel(List<ReporteDto> data) throws IOException {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Stock Actual");

            // Estilos de cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = ((XSSFWorkbook) workbook).createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezado del Reporte (Fecha y Hora)
            Row headerInfoRow = sheet.createRow(0);
            headerInfoRow.createCell(0).setCellValue("Reporte Generado el: " + timestamp);

            // Cabeceras de la Tabla
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de Datos
            int rowIdx = 3;
            for (ReporteDto dto : data) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(dto.getIdProducto());
                row.createCell(1).setCellValue(dto.getNombreProducto());
                row.createCell(2).setCellValue(dto.getCategoria());
                row.createCell(3).setCellValue(dto.getUbicacion());
                row.createCell(4).setCellValue(dto.getStockDisponible());
                row.createCell(5).setCellValue(dto.getStockMinimo());
            }

            // Ajustar el ancho de las columnas
            for (int i = 0; i < HEADERS.length; i++) {
                workbook.getSheetAt(0).autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    /**
     * Genera un archivo PDF con la fecha de generación.
     */
    public ByteArrayOutputStream exportToPdf(List<ReporteDto> data) throws IOException {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate()); // Horizontal

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --- Encabezado y Título ---
            Paragraph title = new Paragraph("Reporte de Stock Actual",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Fecha y Hora
            Paragraph dateInfo = new Paragraph("Generado el: " + timestamp,
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY));
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingAfter(15);
            document.add(dateInfo);

            // --- Tabla de Datos ---
            PdfPTable table = new PdfPTable(HEADERS.length);
            table.setWidthPercentage(100);

            // Cabeceras de la Tabla
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Color headerBg = new Color(52, 73, 94);

            for (String header : HEADERS) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(headerBg);
                headerCell.setPadding(5);
                table.addCell(headerCell);
            }

            // Filas de Datos
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (ReporteDto dto : data) {
                // ID (Centrado)
                PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(dto.getIdProducto()), dataFont));
                idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(idCell);

                table.addCell(new Phrase(dto.getNombreProducto(), dataFont));
                table.addCell(new Phrase(dto.getCategoria(), dataFont));
                table.addCell(new Phrase(dto.getUbicacion(), dataFont));

                // Stock (Centrado)
                PdfPCell stockCell = new PdfPCell(new Phrase(String.valueOf(dto.getStockDisponible()), dataFont));
                stockCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(stockCell);

                PdfPCell minStockCell = new PdfPCell(new Phrase(String.valueOf(dto.getStockMinimo()), dataFont));
                minStockCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(minStockCell);
            }

            document.add(table);

        } catch (Exception e) {
            throw new IOException("Error generando el documento PDF: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return out;
    }
}