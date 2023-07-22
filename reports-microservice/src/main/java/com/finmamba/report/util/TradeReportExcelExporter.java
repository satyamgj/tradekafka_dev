package com.finmamba.report.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.finmamba.models.dto.ReportsDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TradeReportExcelExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<ReportsDto> tradeReport;

	// This class will make a new workbook
	public TradeReportExcelExporter(List<ReportsDto> tradeReport) {
		this.tradeReport = tradeReport;
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine(String sheetName) {
		sheet = workbook.createSheet(sheetName);
		Properties properties = new Properties();
		try {
			 System.out.println("Working Directory = " + System.getProperty("user.dir"));
			properties.load(new FileInputStream(System.getProperty("user.dir")+ "/src/main/resources/tradereport.properties"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		Set<Object> keys = properties.keySet();
		Row row = sheet.createRow(0);
		int columnCount = 0;
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		style.setFont(font);
		List<String> list = new ArrayList (keys);
        Collections.sort(list);
		for (Object key : list /* properties.stringPropertyNames() */) {
			String value = properties.getProperty(key.toString());
			createCell(row, columnCount, value, style);
			System.out.println(key + " => " + value);
			columnCount++;
		}
		/*
		 * createCell(row, columnCount, "Product", style); createCell(row,
		 * columnCount++, "Buy/Sell Direction", style); createCell(row, columnCount++,
		 * "Buy Currency", style); createCell(row, columnCount++, "Sell Currency",
		 * style); createCell(row, columnCount++, "Buy Amount", style); createCell(row,
		 * columnCount++, "Sell Amount", style); createCell(row, columnCount++,
		 * "Spot Rate", style); createCell(row, columnCount++, "Tenor Unit", style);
		 * createCell(row, columnCount++, "Notional", style); createCell(row,
		 * columnCount++, "Source Entity", style); createCell(row, columnCount++,
		 * "Destination Entity", style); createCell(row, columnCount++,
		 * "Transaction Id", style); createCell(row, columnCount++,
		 * "Transaction Timestamp", style);
		 */ }

	private void createCell(Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		Cell cell = row.createCell(columnCount);
		if (value == null) {
			cell.setCellValue((""));
		} else if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Long) {
			cell.setCellValue((Long) value);
		} else if (value instanceof BigDecimal) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Date) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		       
	        // converting the util date into string format
			//java.util.Date utilDate = new java.util.Date(value.getTime());
	        final String stringDate = dateFormat.format(value);
			cell.setCellValue(stringDate);
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
try {
		for (ReportsDto tradeReportRow : tradeReport) {
			Row row = sheet.createRow(rowCount++);
			int columnCount = 0;
	//		System.out.println(" " + rowCount + " => " +columnCount+ " => " + tradeReportRow.getTransactionId().toString());
			
			createCell(row, columnCount++,
					(tradeReportRow.getTransactionId()) != null ? tradeReportRow.getTransactionId() : "", style);
			createCell(row, columnCount++,
					(tradeReportRow.getBrokerName()) != null ? tradeReportRow.getBrokerName() : "", style);
			createCell(row, columnCount++,
					(tradeReportRow.getSourceName()) != null ? tradeReportRow.getSourceName() : "", style);
			createCell(row, columnCount++,
					(tradeReportRow.getCounterpartyTransactionId()) != null
							? tradeReportRow.getCounterpartyTransactionId()
							: "",
					style);
			createCell(row, columnCount++, (tradeReportRow.getName()) != null ? tradeReportRow.getName() : "", style);
			createCell(row, columnCount++, (tradeReportRow.getCreatedAt()) != null ? tradeReportRow.getCreatedAt() : "",
					style);
			createCell(row, columnCount++, (tradeReportRow.getSpread()) != null ? tradeReportRow.getSpread() : 0.0,
					style);
//			System.out.println(" " + rowCount + " => " +columnCount+ " => " + tradeReportRow.getSpread().toString());

			createCell(row, columnCount++, (tradeReportRow.getDirection()) != null ? tradeReportRow.getDirection() : "",
					style);
			createCell(row, columnCount++,
					(tradeReportRow.getBrokerageCcy()) != null ? tradeReportRow.getBrokerageCcy() : "", style);
			createCell(row, columnCount++, (tradeReportRow.getAmount()) != null ? tradeReportRow.getAmount() : 0.0,
					style);
			// destination needs to be added.

			createCell(row, columnCount++,
					(tradeReportRow.getBrokerageBaseCcy()) != null ? tradeReportRow.getBrokerageBaseCcy() : "", style);
			createCell(row, columnCount++,
					(tradeReportRow.getBaseAmount()) != null ? tradeReportRow.getBaseAmount() : 0.0, style);

			createCell(row, columnCount++,
					(tradeReportRow.getCommission()) != null ? tradeReportRow.getCommission() : 0.0, style);
			createCell(row, columnCount++,
					(tradeReportRow.getConversionRate()) != null ? tradeReportRow.getConversionRate() : 0.0, style);

			createCell(row, columnCount++,
					(tradeReportRow.getBankBuyCcyId()) != null ? tradeReportRow.getBankBuyCcyId() : "", style);
			createCell(row, columnCount++,
					(tradeReportRow.getBankSellCcyId()) != null ? tradeReportRow.getBankSellCcyId() : "", style);

			createCell(row, columnCount++,
					(tradeReportRow.getClientOutright()) != null ? tradeReportRow.getClientOutright() : 0.0, style);
			createCell(row, columnCount++,
					(tradeReportRow.getBrokerCommissionModeIsPip()) != null	
					? tradeReportRow.getBrokerCommissionModeIsPip()	: false, style);
			createCell(row, columnCount++,
					(tradeReportRow.getInstrumentType()) != null	
					? tradeReportRow.getInstrumentType() : "", style);
		}
} catch (Exception e) {
	e.printStackTrace();
}
	}

	public void export(HttpServletResponse response, String sheetName) throws IOException {
		
		writeHeaderLine(sheetName);
		writeDataLines();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();

	}
}
