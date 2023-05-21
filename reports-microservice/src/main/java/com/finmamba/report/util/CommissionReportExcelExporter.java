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

import com.finmamba.models.dto.SuperInstitutionalEntityDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CommissionReportExcelExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private List<SuperInstitutionalEntityDto> commissionReport;

	// This class will make a new workbook
	public CommissionReportExcelExporter(List<SuperInstitutionalEntityDto> commissionReport) {
		this.commissionReport = commissionReport;
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine() {
		sheet = workbook.createSheet("CommissionReport");

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
		createCell(row, columnCount++, "Buy Side Spread", style);
		createCell(row, columnCount++, "Sell Side Spread", style);
		createCell(row, columnCount++, "Buy Side Commission", style);
		createCell(row, columnCount++, "Sell Side Commission", style);

	}

	private void createCell(Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		Cell cell = row.createCell(columnCount);
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
			}
		else if (value instanceof BigDecimal) {
				cell.setCellValue((Double) value);
		} else if (value instanceof Long) {
			cell.setCellValue((Long) value);
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

		for (SuperInstitutionalEntityDto commissionReportRow : commissionReport) {
			Row row = sheet.createRow(rowCount++);
			int columnCount = 0;

			createCell(row, columnCount, (commissionReportRow.getProduct())!=null ? commissionReportRow.getProduct().getName():"", style);
			createCell(row, columnCount++, (commissionReportRow.getBuySellDirection())!=null ? commissionReportRow.getBuySellDirection().getName():"", style);
			createCell(row, columnCount++, (commissionReportRow.getBaseCurrency())!=null ? commissionReportRow.getBaseCurrency().getIsoCode():"", style);
			createCell(row, columnCount++, (commissionReportRow.getQuoteCurrency())!=null ?commissionReportRow.getQuoteCurrency().getIsoCode():"", style);
			createCell(row, columnCount++, (commissionReportRow.getBuyAmount())!=null ?commissionReportRow.getBuyAmount().doubleValue():0.0, style);
			createCell(row, columnCount++, (commissionReportRow.getSellAmount())!=null ?commissionReportRow.getSellAmount().doubleValue():0.0, style);
			createCell(row, columnCount++, (commissionReportRow.getSourceSpotRate())!=null ?commissionReportRow.getSourceSpotRate().doubleValue():0.0, style);
			createCell(row, columnCount++, (commissionReportRow.getTenorUnit())!=null ?commissionReportRow.getTenorUnit().getTenorUnit():"", style);
			createCell(row, columnCount++, (commissionReportRow.getNotional())!=null ?commissionReportRow.getNotional().doubleValue():0.0, style);
			createCell(row, columnCount++, (commissionReportRow.getSource())!=null ?commissionReportRow.getSource().getLongName():"", style);
			createCell(row, columnCount++, "NOT AVAILABLE", style);
			createCell(row, columnCount++, (commissionReportRow.getTransactionId())!=null ?commissionReportRow.getTransactionId():0, style);
			createCell(row, columnCount++, "NOT AVAILABLE", style);
			createCell(row, columnCount++, commissionReportRow.getBuySideSpread(), style);
			createCell(row, columnCount++, commissionReportRow.getSellSideSpread(), style);
			createCell(row, columnCount++, commissionReportRow.getCalculatedBuyCommission(), style);
			createCell(row, columnCount++, commissionReportRow.getCalculatedSellCommission(), style);

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
