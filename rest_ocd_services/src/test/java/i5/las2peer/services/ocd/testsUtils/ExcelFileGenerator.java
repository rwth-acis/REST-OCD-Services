package i5.las2peer.services.ocd.testsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFileGenerator {

	public void genFile(Map<String, Object[]> data){
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Data");
		
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for(String key: keyset){
			rownum = Integer.parseInt(key);
			Row row = sheet.createRow(rownum);
			Object [] objectArray = data.get(key);
			int cellnum = 0;
			for(Object o: objectArray){
				Cell c = row.createCell(cellnum++);
				if(o instanceof String){
					c.setCellValue((String) o); 
				}
				if(o instanceof Double){
					c.setCellValue((Double) o);
				}
				if(o instanceof Integer){
					c.setCellValue((Integer) o);
				}
			}
		}
		try{
			FileOutputStream out = new FileOutputStream(new File("Evaluation.xlsx"));
			workbook.write(out);
			out.close();
			System.out.println("Excel file written");
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
