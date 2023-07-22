package com.finmamba.nonflow.util;

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

import com.finmamba.models.reports.NonFlowReportDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class NonFlowReportExcelExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<NonFlowReportDto> nonFlowReport;

	// This class will make a new workbook
	public NonFlowReportExcelExporter(List<NonFlowReportDto> nonFlowReport) {
		this.nonFlowReport = nonFlowReport;
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine(String sheetName) {
		sheet = workbook.createSheet(sheetName);
		Properties properties = new Properties();
		try {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			properties.load(new FileInputStream(
					System.getProperty("user.dir") + "/src/main/resources/nonflow.properties"));
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
		List<String> list = new ArrayList(keys);
		Collections.sort(list);
		for (Object key : list /* properties.stringPropertyNames() */) {
			String value = properties.getProperty(key.toString());
			createCell(row, columnCount, value, style);
			System.out.println(key + " => " + value);
			columnCount++;
		}
	}

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
			// java.util.Date utilDate = new java.util.Date(value.getTime());
			final String stringDate = dateFormat.format(value);
			cell.setCellValue(stringDate);
		} else {
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
			for (NonFlowReportDto nonFlowReportRow : nonFlowReport) {
				Row row = sheet.createRow(rowCount++);
				int columnCount = 0;
				// System.out.println(" " + rowCount + " => " +columnCount+ " => " +
				// tradeReportRow.getTransactionId().toString());

				createCell(row, columnCount++,
						(nonFlowReportRow.getTransactionId()) != null ? nonFlowReportRow.getTransactionId() : "",
						style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getBrokerName()) != null ? nonFlowReportRow.getBrokerName() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getSourceName()) != null ? nonFlowReportRow.getSourceName() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getCounterpartyTransactionId()) != null
								? nonFlowReportRow.getCounterpartyTransactionId()
								: "",
						style);
				createCell(row, columnCount++, (nonFlowReportRow.getName()) != null ? nonFlowReportRow.getName() : "",
						style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getCreatedAt()) != null ? nonFlowReportRow.getCreatedAt() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getSpread()) != null ? nonFlowReportRow.getSpread() : 0.0, style);
//			System.out.println(" " + rowCount + " => " +columnCount+ " => " + nonFlowReportRow.getSpread().toString());

				createCell(row, columnCount++,
						(nonFlowReportRow.getDirection()) != null ? nonFlowReportRow.getDirection() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getBrokerageCcy()) != null ? nonFlowReportRow.getBrokerageCcy() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getAmount()) != null ? nonFlowReportRow.getAmount() : 0.0, style);
				// destination needs to be added.

				createCell(row, columnCount++,
						(nonFlowReportRow.getBrokerageBaseCcy()) != null ? nonFlowReportRow.getBrokerageBaseCcy() : "",
						style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getBaseAmount()) != null ? nonFlowReportRow.getBaseAmount() : 0.0, style);

				createCell(row, columnCount++,
						(nonFlowReportRow.getCommission()) != null ? nonFlowReportRow.getCommission() : 0.0, style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getConversionRate()) != null ? nonFlowReportRow.getConversionRate() : 0.0,
						style);

				createCell(row, columnCount++,
						(nonFlowReportRow.getBankBuyCcyId()) != null ? nonFlowReportRow.getBankBuyCcyId() : "", style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getBankSellCcyId()) != null ? nonFlowReportRow.getBankSellCcyId() : "",
						style);

				createCell(row, columnCount++,
						(nonFlowReportRow.getClientOutright()) != null ? nonFlowReportRow.getClientOutright() : 0.0,
						style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getBrokerCommissionModeIsPip()) != null
								? nonFlowReportRow.getBrokerCommissionModeIsPip()
								: false,
						style);
				createCell(row, columnCount++,
						(nonFlowReportRow.getInstrumentType()) != null ? nonFlowReportRow.getInstrumentType() : "",
						style);
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
