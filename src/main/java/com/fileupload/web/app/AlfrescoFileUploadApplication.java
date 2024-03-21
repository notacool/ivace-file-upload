package com.fileupload.web.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;

public class AlfrescoFileUploadApplication {

	public static void main(String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"C:\\Users\\User\\Documents\\Projects\\ivace-file-upload\\src\\main\\java\\com\\fileupload\\web\\app\\jpg_fin_V1.0.txt"));
			ArrayList<String> array = new ArrayList<String>();
			String size = "";
			String reference = "";
			String dim = "";
			Integer i = -1;
			String line = reader.readLine();
			while (line != null) {
				i++;
				String[] lineSplitted = line.split(" ");
				if (line.contains("cargado")) {
					reference = lineSplitted[0];
					dim = lineSplitted[1];
				} else if (line.contains("guardado")) {
					line = reader.readLine();
					continue;
				} else if (line.equals("")) {
					line = reader.readLine();
					continue;
				} else {
					size = line.split("-")[0];
				}
				if (i % 3 == 2) {
					array.add(reference + "," + dim + "," + size);
				}
				line = reader.readLine();
			}
			reader.close();

			Integer index = 0;
			for (String s : array) {
				String filePath = s.split(",")[0];
				String[] pathSplitted = filePath.split("\\\\");
				String fileName = pathSplitted[pathSplitted.length - 2];
				String fullPath = "C:\\Users\\User\\Documents\\Projects\\Converted files\\" + fileName
						+ ".xlsx";

				try {
					FileInputStream fis = new FileInputStream(new File(fullPath));
					Workbook workbook = new XSSFWorkbook(fis);
					Sheet sheet = workbook.getSheetAt(0);
					for (Row row : sheet) {
						if (row.getRowNum() == 0) {
							continue;
						}
						String name = s.split(",")[0].split("\\\\")[s.split(",")[0].split("\\\\").length - 1]
								.replaceAll(".tif", "");
						if (row.getCell(0).getStringCellValue().equals(name)) {
							row.createCell(5).setCellValue(s.split(",")[1]);
							row.createCell(4).setCellValue(s.split(",")[2]);
						}
					}
					fis.close();

					FileOutputStream fos = new FileOutputStream(new File(fullPath));
					workbook.write(fos);
					fos.close();

					workbook.close();
					System.out.println(index.toString() + "file done");
					index++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
