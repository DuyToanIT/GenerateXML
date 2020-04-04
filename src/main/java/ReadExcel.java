import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ReadExcel {
    public Map<String,TableEntry> readListTable(String pathFile) throws IOException {
        Map<String, TableEntry> tableMap = new HashMap<>();

        FileInputStream inputStream = new FileInputStream(new File(pathFile));

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        TableEntry tableEntry = new TableEntry();
        Iterator<Row> rows = sheet.iterator();
        rows.next();
        String r = sheet.getRow(1).getCell(4).getStringCellValue();
        int numRowEmtry = 0;
        while (rows.hasNext() && numRowEmtry != 2) {
            Row next = rows.next();
            tableEntry = new TableEntry();
            if(next.getCell(1) != null){
                tableEntry.setTableName(next.getCell(1).getStringCellValue());
                tableEntry.setCharTableJP(r);
            }

            if(next.getCell(2) != null){
                tableEntry.setTableNameJP(next.getCell(2).getStringCellValue());
            }

            if(next.getCell(3) != null){
                tableEntry.setClassName(next.getCell(3).getStringCellValue());
            }
            tableMap.put(tableEntry.getClassName(), tableEntry);
        }
        return tableMap;
    }

    public Map<String, TableObj> read(String pathFile) throws IOException {
        Map<String, TableObj> tableMap = new HashMap<>();
//        Path path = Paths.get(pathFile);
//        File tmp = new File("1.xlsx");
//        path.toFile().renameTo(tmp);
        FileInputStream inputStream = new FileInputStream(pathFile);

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        int totalSheet = workbook.getNumberOfSheets();

        for (int i = 2; i< totalSheet; i++){
            Sheet  sheet = workbook.getSheetAt(i);
            TableObj tableObj = new TableObj();
           // tableMap.put(sheet.getSheetName(), tableObj);
            Iterator<Row> rows = sheet.iterator();
            rows.next();
            rows.next();
            rows.next();
            int numRowEmtry = 0;
            int indexRow = 0;
            while (rows.hasNext() && numRowEmtry != 2) {
                Row next = rows.next();
                RowObj rowObj = new RowObj();
                if (next.getCell(1) != null && !next.getCell(1).getStringCellValue().isEmpty()){
                    tableObj = new TableObj();
                    indexRow = 0;
                    tableObj.setTableName(next.getCell(1).getStringCellValue());
                    tableMap.put(sheet.getSheetName() + "_" + next.getCell(1).getStringCellValue(), tableObj);
                }
                if (next.getCell(2) != null && !next.getCell(2).getStringCellValue().isEmpty()) {
                    rowObj.setIndex(indexRow++);
                    rowObj.setColName(next.getCell(2).getStringCellValue());
                    rowObj.setColTag(next.getCell(3).getStringCellValue());
                    rowObj.setColNameJP(next.getCell(4).getStringCellValue());
                    rowObj.setColType(next.getCell(5).getStringCellValue());

                    StringBuffer stringBuffer = new StringBuffer();
                    // CellValue cellValue = evaluator.evaluate(next.getCell(7));

                    stringBuffer.append(evaluator.evaluate(next.getCell(7)).getStringValue() == null ? "0" : "1");
                    stringBuffer.append(evaluator.evaluate(next.getCell(8)).getStringValue().isEmpty() ? "0" : "1");
                    stringBuffer.append(evaluator.evaluate(next.getCell(9)).getStringValue() == null ? "0" : "1");
                    stringBuffer.append(evaluator.evaluate(next.getCell(10)).getStringValue() == null ? "0" : "1");
                    rowObj.setCaseGenerate(stringBuffer.toString());
                    tableObj.getRowObjs().add(rowObj);
                    numRowEmtry = 0;
                }else{
                    numRowEmtry++;
                    indexRow = 0;
                }
            }

        }//end read workbook

       /// workbook.getNumberOfSheets()
        return tableMap;
    }

    public Map<String, String> readFileInput(String path) throws IOException {
        Map<String, String> lstPath = new HashMap<>();
        FileInputStream fis = new FileInputStream(path);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis, "UTF8"));
        String line;
        while ((line = in.readLine()) != null){
            lstPath.put(line.split(" ")[0], line.split(" ")[1]);
        }
        return lstPath;
    }
}
