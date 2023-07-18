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

import com.finmamba.models.dto.ConfirmedOrderDto;
import com.finmamba.models.dto.ReportsDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TradeReportExcelExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<ReportsDto> tradeReport;

	// This class will make a new workbook
	public TradeReportExcelExporter(List<ReportsDto> tradeReport) {
		this.tradeReport = tradeReport;
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine() {
		sheet = workbook.createSheet("TradeReport");

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
		int rowCount = 1;

		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		style.setFont(font);

		for (ReportsDto tradeReportRow : tradeReport) {
			Row row = sheet.createRow(rowCount++);
			int columnCount = 0;

			/*
			 * createCell(row, columnCount, (tradeReportRow.getProduct())!=null ?
			 * tradeReportRow.getProduct().getName():"", style); createCell(row,
			 * columnCount++, (tradeReportRow.getBuySellDirection())!=null ?
			 * tradeReportRow.getBuySellDirection().getName():"", style); createCell(row,
			 * columnCount++, (tradeReportRow.getBaseCurrency())!=null ?
			 * tradeReportRow.getBaseCurrency().getIsoCode():"", style); createCell(row,
			 * columnCount++, (tradeReportRow.getQuoteCurrency())!=null
			 * ?tradeReportRow.getQuoteCurrency().getIsoCode():"", style); createCell(row,
			 * columnCount++, (tradeReportRow.getBuyAmount())!=null
			 * ?tradeReportRow.getBuyAmount().doubleValue():0.0, style); createCell(row,
			 * columnCount++, (tradeReportRow.getSellAmount())!=null
			 * ?tradeReportRow.getSellAmount().doubleValue():0.0, style); createCell(row,
			 * columnCount++, (tradeReportRow.getSourceSpotRate())!=null
			 * ?tradeReportRow.getSourceSpotRate().doubleValue():0.0, style);
			 * 
			 * createCell(row, columnCount++, (tradeReportRow.getTenorUnit())!=null
			 * ?tradeReportRow.getTenorUnit().getTenorUnit():"", style); createCell(row,
			 * columnCount++, (tradeReportRow.getNotional())!=null
			 * ?tradeReportRow.getNotional().doubleValue():0.0, style); createCell(row,
			 * columnCount++, (tradeReportRow.getSource())!=null
			 * ?tradeReportRow.getSource().getLongName():"", style);
			 */
		
			// destination needs to be added.
			
			createCell(row, columnCount++, "NOT AVAILABLE", style);
			createCell(row, columnCount++, tradeReportRow.getTransactionId(), style);
			createCell(row, columnCount++, "NOT AVAILABLE", style);
			
		

		}
	}

	public void export(HttpServletResponse response) throws IOException {
		writeHeaderLine();
		writeDataLines();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();

		outputStream.close();

	}
}
