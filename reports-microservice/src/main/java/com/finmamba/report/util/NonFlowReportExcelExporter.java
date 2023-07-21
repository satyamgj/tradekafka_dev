package com.finmamba.report.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.finmamba.models.reports.SaveNonFlowDto;
import com.finmamba.models.reports.SearchNonFlowDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NonFlowReportExcelExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<SearchNonFlowDto> nonFlowReport;

	// This class will make a new workbook
	public NonFlowReportExcelExporter(List<SearchNonFlowDto> nonFlowReport) {
		this.nonFlowReport = nonFlowReport;
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine() {
		sheet = workbook.createSheet("NonFlowReport");

		Row row = sheet.createRow(0);
		int columnCount = 0;
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		style.setFont(font);
		createCell(row, columnCount, "Product", style);
		createCell(row, columnCount++, "Buy/Sell Direction", style);
		createCell(row, columnCount++, "Buy Currency", style);
		createCell(row, columnCount++, "Sell Currency", style);
		createCell(row, columnCount++, "Buy Amount", style);
		createCell(row, columnCount++, "Sell Amount", style);
		createCell(row, columnCount++, "Spot Rate", style);
		createCell(row, columnCount++, "Tenor Unit", style);
		createCell(row, columnCount++, "Notional", style);
		createCell(row, columnCount++, "Source Entity", style);
		createCell(row, columnCount++, "Destination Entity", style);
		createCell(row, columnCount++, "Transaction Id", style);
		createCell(row, columnCount++, "Transaction Timestamp", style);
	}

	private void createCell(Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		Cell cell = row.createCell(columnCount);
		if (value == null) {
			cell.setCellValue((""));
		}
		else if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Long) {
			cell.setCellValue((Long) value);
		}else if (value instanceof BigDecimal) {
			cell.setCellValue((Double) value);
		}
		else {
			cell.setCellValue((String) value);
		}
		cell.setCellStyle(style);
	}

	private void writeDataLines() {
		/*
		 * int rowCount = 1;
		 * 
		 * CellStyle style = workbook.createCellStyle(); XSSFFont font =
		 * workbook.createFont(); font.setFontHeight(14); style.setFont(font);
		 * 
		 * for (SearchNonFlowDto nonFlowReportRow : nonFlowReport) { Row row =
		 * sheet.createRow(rowCount++); int columnCount = 0;
		 * 
		 * createCell(row, columnCount, (nonFlowReportRow.getProductId())!=null ?
		 * nonFlowReportRow.getProductId().getName():"", style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getBuySellDirection())!=null ?
		 * nonFlowReportRow.getBuySellDirection().getName():"", style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getBaseCurrencyId())!=null ?
		 * nonFlowReportRow.getBaseCurrencyId().getIsoCode():"", style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getQuoteCurrency())!=null
		 * ?nonFlowReportRow.getQuoteCurrency().getIsoCode():"", style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getBuyAmount())!=null
		 * ?nonFlowReportRow.getBuyAmount().doubleValue():0.0, style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getSellAmount())!=null
		 * ?nonFlowReportRow.getSellAmount().doubleValue():0.0, style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getSpotRate())!=null
		 * ?nonFlowReportRow.getSpotRate().doubleValue():0.0, style);
		 * 
		 * createCell(row, columnCount++, (nonFlowReportRow.getTenorUnitId())!=null
		 * ?nonFlowReportRow.getTenorUnitId().getTenorUnit():"", style);
		 * //createCell(row, columnCount++, (nonFlowReportRow.getNotional())!=null
		 * ?nonFlowReportRow.getNotional().doubleValue():0.0, style); createCell(row,
		 * columnCount++, (nonFlowReportRow.getInstitutionalSourceEntityId())!=null
		 * ?nonFlowReportRow.getInstitutionalSourceEntityId().getLongName():"", style);
		 * 
		 * // destination needs to be added.
		 * 
		 * createCell(row, columnCount++, "NOT AVAILABLE", style); //createCell(row,
		 * columnCount++, nonFlowReportRow.getTransactionId(), style); createCell(row,
		 * columnCount++, "NOT AVAILABLE", style);
		 * 
		 * }
		 */}

	public void export(HttpServletResponse response) throws IOException {
		writeHeaderLine();
		writeDataLines();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();

		outputStream.close();

	}
}
