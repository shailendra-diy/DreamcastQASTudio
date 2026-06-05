package com.dreamcast.automation.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataProviderUtil {

    /**
     * Reads an Excel sheet and returns data as Object[][] for TestNG @DataProvider.
     * First row is treated as header and skipped.
     *
     * @param filePath  path to .xlsx file
     * @param sheetName name of the sheet to read
     */
    public static Object[][] readExcel(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook   = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null)
                throw new RuntimeException("Sheet not found: " + sheetName);

            int rows = sheet.getLastRowNum();   // excludes header row
            int cols = sheet.getRow(0).getLastCellNum();

            List<Object[]> data = new ArrayList<>();
            for (int r = 1; r <= rows; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Object[] rowData = new Object[cols];
                for (int c = 0; c < cols; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData[c] = getCellValue(cell);
                }
                data.add(rowData);
            }
            return data.toArray(new Object[0][]);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + e.getMessage());
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default:      return "";
        }
    }
}
