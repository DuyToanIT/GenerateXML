import enums.StateCol;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Main {

    private static Map<String,TableObj> tables;
    private static Map<String,TableEntry>tableEntry;
    private static String STATIC_SU = "su";
    private static String STATIC_ARY = "ary";

    public static void main(String[] args) throws IOException {
        ReadExcel readExcel = new ReadExcel();
        ///read file
        tableEntry = readExcel.readListTable("10.xlsx");
        Map<String, String> lstPath = readExcel.readFileInput("inputPath.txt");
        for (String tablePath : lstPath.keySet()){

            tables = readExcel.read(lstPath.get(tablePath));

            //   TableObj mainTable = tables.get("DAccdntPnt");
            //TableObj mainTable = tables.get("DAccdntSection");
            String sheetName = tablePath;
            String tableName = tablePath;
            TableObj mainTable = tables.get(sheetName + "_" + tableName);
            StringBuffer stringBuffer = new StringBuffer();
            genStartStoredProcedure(sheetName, stringBuffer, mainTable);
            genEndStoredProcedure(stringBuffer);
            //  System.out.println(stringBuffer.toString());
            writeStoredProcedure(mainTable.getTableName()+".txt", stringBuffer.toString());
        }

        //tables = readExcel.read("C:/Users/toan.nd/Documents/Project/GenerateXML/src/main/resources/D_DACCDNTPNT.xlsx");
       //tables = readExcel.read("C:/Users/toan.nd/Documents/Project/GenerateXML/src/main/resources/D_DACCDNTSECTION.xlsx");
       // tables = readExcel.read("C:/Users/toan.nd/Documents/Project/GenerateXML/src/main/resources/D_TGNRPOI.xlsx");
       // tables = readExcel.read("C:/Users/toan.nd/Documents/Project/GenerateXML/src/main/resources/D_DFRRYPORT05.xlsx");
//        tables = readExcel.read("C:/Users/toan.nd/Documents/Project/GenerateXML/src/main/resources/D_TPOINOPNT.xlsx");
//
//     //   TableObj mainTable = tables.get("DAccdntPnt");
//        //TableObj mainTable = tables.get("DAccdntSection");
//        String sheetName = "TPOINoPnt";
//        String tableName = "TPOINoPnt";
//        TableObj mainTable = tables.get(sheetName + "_" + tableName);
//        StringBuffer stringBuffer = new StringBuffer();
//        genStartStoredProcedure(sheetName, stringBuffer, mainTable);
//        genEndStoredProcedure(stringBuffer);
//      //  System.out.println(stringBuffer.toString());
//        writeStoredProcedure(mainTable.getTableName()+".txt", stringBuffer.toString());
    }

    public static void genStartStoredProcedure(String sheetName, StringBuffer sql, TableObj mainTable) throws UnsupportedEncodingException {
        sql.append("SET ANSI_NULLS ON\r\n");
        sql.append("GO\r\n");
        sql.append("SET QUOTED_IDENTIFIER ON\r\n");
        sql.append("GO\r\n");
        sql.append("-- =============================================\r\n");
        sql.append("-- "+tableEntry.get(mainTable.getTableName()).getCharTableJP()+" "+ tableEntry.get(mainTable.getTableName()).getTableName() +" "+ tableEntry.get(mainTable.getTableName()).getTableNameJP()+"\r\n");
        sql.append("-- =============================================\r\n");
        sql.append("CREATE PROCEDURE "+"[dbo].[SP_create_D_"+ mainTable.getTableName().toUpperCase() + "]\r\n");
        sql.append("(\r\n");
        sql.append("  @db nvarchar(max)\r\n");
        sql.append(")\r\n");
        sql.append("AS\r\n");
        sql.append("BEGIN\r\n");
        sql.append("\tDECLARE @sql nvarchar(max) = '';\r\n");
        sql.append("\tDECLARE @param nvarchar(max) = '';\r\n\r\n");
        sql.append("\tTRUNCATE TABLE D_"+ mainTable.getTableName().toUpperCase()+";\r\n\r\n");
        sql.append("\tSET @sql = N'\r\n");
        genInsert(sql, mainTable.getTableName());
        genUpdate(sheetName,sql, mainTable);
    }

    public static void genEndStoredProcedure(StringBuffer sql){
        sql.append("\r\n\tEXECUTE sp_executesql @sql, @param;\r\n\r\n");
        sql.append("END;");
    }

    public static void genInsert(StringBuffer sql, String tableName){
        sql.append("\tINSERT INTO D_"+ tableName.toUpperCase()+"\r\n");
        sql.append("\t( "+getColId(tableName)+", XMLProperty )\r\n");
        sql.append("\tSELECT\r\n");
        sql.append("\t  T."+getColId(tableName)+"\r\n\t, ''''\r\n");
        sql.append("\tFROM\r\n");
        sql.append("\t  [' + @db + '].dbo.R_D_"+tableName.toUpperCase() + " T;';\r\n");
        sql.append("\r\n\tEXECUTE sp_executesql @sql, @param;\r\n\r\n");
    }

    public static void genUpdate(String sheetName, StringBuffer sql, TableObj mainTable){
        sql.append("\tSET @sql =N'\r\n");
        sql.append("\tUPDATE T\r\n");
        sql.append("\tSET\r\n");
        sql.append("\t  T.XMLProperty = (\r\n");
        sql.append("\t\tSELECT\r\n");
        String tabSelect = "\t\t";
//        sql.append("\t T." +getColId(mainTable.getTableName())+ "\r\n");
//        sql.append("\t, ( SELECT \r\n");
        for(RowObj rowObj: mainTable.getRowObjs()){
            StateCol type = StateCol.FIRST;
            boolean isGenCondition = false;
            if (rowObj.getIndex() == 0){
                type = StateCol.FIRST;
            }else if(rowObj.getIndex() == mainTable.getRowObjs().size() -1){
                type = StateCol.END;
                //isGenCondition = true;
            }else{
                type = StateCol.DIFFFIST;
            }
            sql.append(caseGen(sheetName, mainTable.getTableName(), rowObj, 1, true, tabSelect, type, ""));
            sql.append("\r\n");
        }
       // sql.append("\t\t) AS XMLProperty\r\n");
        sql.append("\t)\r\n");
        sql.append("\tFROM\r\n");
        sql.append("\t  D_"+mainTable.getTableName().toUpperCase() + " T;';\r\n");
    }
    public static String getColId(String tableName){
        char firstChar = tableName.charAt(0);
        String colId = "";
        switch (firstChar){
            case 'D':
                colId = "DependanceEntityId";
                break;
            case 'S':
                colId = "SpatialEntityId";
                break;
            case 'T':
                colId = "CommunionEntityId";
                break;
        }
        return colId;
    }

    public static String caseGen(String sheetName, String tableName, RowObj rowObj, int levelChild, boolean isGenCondition, String tabSelect, StateCol stateCol, String tagParent){
        String tagP = tagParent.replace("_", "/");
        String caseRDB = rowObj.getCaseGenerate();
        StringBuffer sql = new StringBuffer();
        switch (caseRDB){
            case "0100":
                StringBuffer str = new StringBuffer();
                if (rowObj.getIndex() == 0){
                    sql.append(tabSelect + "  ");
                }else{
                    sql.append(tabSelect + " ,");
                }
                ///////////////////////////////////OLD//////////////////////////////////////////////////////

//                str.append("CASE ");
//                str.append("WHEN TRIM(T" + levelChild + "." + rowObj.getColName() + "Kj) != '''' THEN (SELECT ''ja-Jpan'' AS [A], T" + levelChild + "." + rowObj.getColName() + "Kj AS [F] FOR XML PATH(''d''), TYPE) END AS [" + rowObj.getColTag() + "/A]\r\n" );
//                str.append(tabSelect + " ,CASE\r\n");
//                str.append(tabSelect + "\tWHEN TRIM(T" + levelChild + "." + rowObj.getColName() + "VoiceAware) != '''' THEN (\r\n");
//                str.append(tabSelect+ "\tSELECT\r\n");
//                str.append(tabSelect+ "\t  ''ja-Kana'' AS [A]\n");
//                str.append(tabSelect +"\t ,T" + levelChild + "." + rowObj.getColName() + "Kn AS [F]\r\n");
//                str.append(tabSelect +"\t ,T" + levelChild + "." + rowObj.getColName() + "VoiceAware AS [D/d]\r\n");
//                str.append(tabSelect +"\t ,T" + levelChild + "." + rowObj.getColName() + "VoiceComp AS [E/d]\r\n");
//                str.append(tabSelect + "\tFOR XML PATH(''d''), TYPE\r\n");
//                str.append(tabSelect + "\t)\r\n");
//                str.append(tabSelect + "\tWHEN TRIM(T" + levelChild + "." + rowObj.getColName() + "Kn) != '''' THEN (\r\n");
//                str.append(tabSelect+ "\tSELECT\r\n");
//                str.append(tabSelect+ "\t  ''ja-Kana'' AS [A]\n");
//                str.append(tabSelect +"\t ,T" + levelChild + "." + rowObj.getColName() + "Kn AS [F]\r\n");
//                str.append(tabSelect + "\tFOR XML PATH(''d''), TYPE\r\n");
//                str.append(tabSelect + "\t)\r\n");
//                str.append(tabSelect + "\tEND AS [" + rowObj.getColTag() + "/A]\r\n");

                //////////////////////////////////new ////////////////////////////////////////////
                str.append("(SELECT\r\n");
                str.append(tabSelect + "  \tT" + (levelChild+1) + ".IETFLangTag AS [A]\r\n");
                str.append(tabSelect + " ,\tT" + (levelChild+1) + ".Name AS [F]\r\n");
                str.append(tabSelect + "FROM\r\n");
                str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild+1) +"\r\n");
                str.append(tabSelect+"WHERE\r\n");
                str.append(tabSelect+ "  T." + getColId(tableName) + " = T" + (levelChild+1) +"."+ getColId(tableName) + " AND T"+(levelChild+1)+".rec = 1\r\n");
                str.append(tabSelect + "FOR XML PATH(''d''), TYPE) AS ["+rowObj.getColTag() + "/A]\r\n");

                str.append(tabSelect+ ", (SELECT\r\n");
                str.append(tabSelect + "  \tT" + (levelChild+1) + ".IETFLangTag AS [A]\r\n");
                str.append(tabSelect + " ,\tT" + (levelChild+1) + ".Name AS [F]\r\n");
                str.append(tabSelect + " ,\tT" + (levelChild+1) + ".VoiceAwareAry AS [D/d]\r\n");
                str.append(tabSelect + " ,\tT" + (levelChild+1) + ".VoiceCompAry AS [E/d]\r\n");
                str.append(tabSelect + "FROM\r\n");
                str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild+1) +"\r\n");
                str.append(tabSelect + "WHERE\r\n");
                str.append(tabSelect+ "  T." + getColId(tableName) + " = T" + (levelChild+1) +"."+ getColId(tableName) + " AND T"+(levelChild+1)+".rec = 2\r\n");
                str.append(tabSelect + "FOR XML PATH(''d''), TYPE) AS ["+rowObj.getColTag() + "/A]\r\n");

                str.append(tabSelect + ", (SELECT\r\n");
                str.append(tabSelect + "  \tT" + (levelChild+1) + ".IETFLangTag AS [A]\r\n");
                str.append(tabSelect + " ,\tT" + (levelChild+1) + ".Name AS [F]\r\n");
                str.append(tabSelect + "FROM\r\n");
                str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild+1) +"\r\n");
                str.append(tabSelect + "WHERE\r\n");
                str.append(tabSelect+ "  T." + getColId(tableName) + " = T" + (levelChild+1) +"."+ getColId(tableName) + " AND T"+(levelChild+1)+".rec = 3\r\n");
                str.append(tabSelect + "FOR XML PATH(''d''), TYPE) AS ["+rowObj.getColTag() + "/A]\r\n");

                ////////////////////////////////////////NHAP//////////////////////////////////////////////

              /*  str.append("''ja-Jpan'' AS ["+ rowObj.getColTag()+"/A/d/A]\r\n");
                str.append(tabSelect + ", T"+ levelChild + "." + rowObj.getColName()+ "Kj AS [" + rowObj.getColTag()+"/A/d/F]\r\n");
                str.append(tabSelect + ", CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "Kn )!= '''' OR CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "VoiceAware )!= '''' OR CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "VoiceComp )!= '''' THEN ''ja-Kana'' END AS ["+ rowObj.getColTag()+"/A/d2/A]\r\n");
                //str.append(tabSelect + ", ''ja-Kana'' AS ["+ rowObj.getColTag()+"/A/d2/A]\r\n");
                str.append(tabSelect + ", CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "Kn )!= '''' THEN T"+ levelChild + "." + rowObj.getColName()+ "Kn END AS [" + rowObj.getColTag()+"/A/d2/F]\r\n");
                str.append(tabSelect + ", CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "VoiceAware )!= '''' THEN T" + levelChild+ "." + rowObj.getColName() + "VoiceAware END AS ["+ rowObj.getColTag()+"/A/d2/D/d]\r\n");
                str.append(tabSelect + ", CASE WHEN TRIM(T"+ levelChild + "." + rowObj.getColName()+ "VoiceComp )!= '''' THEN T" + levelChild+ "." + rowObj.getColName() + "VoiceComp END AS ["+ rowObj.getColTag()+"/A/d2/E/d]\r\n");*/
                if (isGenCondition)
                    genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, str, tableName, rowObj, levelChild, tabSelect);
                sql.append(str);
                break;
            case "0101":
                str = new StringBuffer();


                break;
            case "0000":
                str = new StringBuffer();
                TableObj chilTable = tables.get(sheetName + "_"+ rowObj.getColType());
                String tabSelectChil = "";
                tagParent = tagParent.isEmpty() ? rowObj.getColTag() : tagParent + "/" + rowObj.getColTag();
                for(RowObj rowChilObj: chilTable.getRowObjs()){
                    if (stateCol.equals(StateCol.FIRST)){
                        str.append(tabSelect+"  ");
                        stateCol = StateCol.DIFFFIST;
                    }else if (stateCol.equals(StateCol.DIFFFIST)){
                        str.append(tabSelect+", ");
                    }
                   // String tagChil = tagParent.isEmpty() ? rowChilObj.getColTag() : tagParent + "/" + rowChilObj.getColTag();
                    str.append("T"+ levelChild + "." + rowObj.getColName()+ caseGen(sheetName, chilTable.getTableName(),rowChilObj, 1,false, tabSelectChil, StateCol.CONCAT, tagParent));
                    str.append("\r\n");
                }
//                String parentId = levelChild == 1 ? "T."+ getColId(tableName) : "T"+ (levelChild -1)+ "." + getColId(tableName);
//                String chilId = "T"+ levelChild+ "." + getColId(tableName);
//                str.append("\r\n"+tabSelect+"FROM\r\n");
//                str.append(tabSelect+"  [' + @db + '].dbo.R_D_"+tableName + " T"+ levelChild +"\r\n\r\n");
//                str.append(tabSelect+"WHERE\r\n");
//                str.append(tabSelect+"  "+ parentId + " = " + chilId+"\r\n");
//                str.append(tabSelect+"FOR XML PATH(''" + rowObj.getColTag()+ "''), ROOT(''r'')");
                if (isGenCondition){
                    genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, str, tableName, rowObj, levelChild, tabSelect);

                  //  genConditions(sheetName, str, tableName, rowObj, levelChild, tabSelect);
                }
                sql.append(str);
                break;
            case "0011":
                str = new StringBuffer();
                if (stateCol.equals(StateCol.FIRST)){
                    str.append(tabSelect+"  ");
                    stateCol = StateCol.DIFFFIST;
                }else if (stateCol.equals(StateCol.DIFFFIST)){
                    str.append(tabSelect+", ");
                }
                str.append("(SELECT\r\n");
                str.append(tabSelect+"\t  T"+(levelChild) +".reftype AS [d/@reftype]\r\n");
                str.append(tabSelect+"\t, T"+(levelChild) +".ref AS [d/@ref]\r\n");
                str.append(tabSelect+"  FROM\r\n");
                str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild) +"\r\n");
                str.append(tabSelect+"  WHERE\r\n");
                str.append(tabSelect+ "  T." + getColId(tableName) + " = T" + (levelChild) +"."+ getColId(tableName) + "\r\n");
                if (tables.get(sheetName+"_"+tableName).getRowObjs().size() == 1){
                    str.append(tabSelect+ "  FOR XML PATH(''"+rowObj.getColTag()+"''), ROOT(''r'')\r\n");
                }else{
                    str.append(tabSelect+ "  FOR XML PATH(''''), TYPE\r\n");
                }
                str.append(tabSelect+") ["+rowObj.getColTag() + "]\r\n");
                sql.append(str);
                break;
            case "0001":
                str = new StringBuffer();
                if (stateCol.equals(StateCol.FIRST)){
                    str.append(tabSelect+"  ");
                    stateCol = StateCol.DIFFFIST;
                }else if (stateCol.equals(StateCol.DIFFFIST)){
                    str.append(tabSelect+", ");
                }
                String tag = tagParent.isEmpty() ? rowObj.getColTag() : tagParent + "_" + rowObj.getColTag();
                tagParent = tag;
                chilTable = tables.get(sheetName + "_" + tag + "_" + rowObj.getColType());
                tabSelectChil = "";
                if(rowObj.getIndex() > 0){
                    str.append(tabSelect + ", ");
                }else{
                    str.append(tabSelect + "  ");
                }
                str.append("(SELECT\r\n");


                for(RowObj rowChilObj: chilTable.getRowObjs()){
                    if (stateCol.equals(StateCol.FIRST)){
                        str.append(tabSelect+"  ");
                        stateCol = StateCol.DIFFFIST;
                    }else if (stateCol.equals(StateCol.DIFFFIST)){
                        str.append(tabSelect+", ");
                    }
                    str.append(tabSelect+ "\t"+caseGen(sheetName, chilTable.getTableName(),rowChilObj, levelChild +1,false, tabSelectChil, stateCol, tagParent));
                    str.append("\r\n");
                }

                str.append(tabSelect+"  FROM\r\n");
                str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild+1) +"\r\n");
                str.append(tabSelect+"  WHERE\r\n");
                str.append(tabSelect+ "  T" + levelChild + "." + getColId(tableName) + " = T" + (levelChild + 1) +"."+ getColId(tableName) + "\r\n");
                str.append(tabSelect+ "  FOR XML PATH(''"+rowObj.getColTag()+"''), ROOT(''d''), TYPE\r\n");
                str.append(tabSelect+") ["+rowObj.getColTag() + "]\r\n");
                if (isGenCondition)
                    genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, str, tableName, rowObj, levelChild, tabSelect);

           //     genConditions(sheetName, str, tableName, rowObj, levelChild, tabSelect);
                sql.append(str);
                break;
            case "0010":
                str = new StringBuffer();
                if (rowObj.getIndex() == 0){
                    str.append(tabSelect + "  ");
                }else{
                    str.append(tabSelect + ", ");
                }
                str.append("T"+ levelChild + ".reftype" + rowObj.getColName()+ " AS [" + rowObj.getColTag()+"/@reftype]\r\n");
                str.append(tabSelect + ", T"+ levelChild + ".ref" + rowObj.getColName()+ " AS [" + rowObj.getColTag()+"/@ref]\r\n");
                if (isGenCondition)
                    genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, str, tableName, rowObj, levelChild, tabSelect);

 //               genConditions(sheetName, str, tableName, rowObj, levelChild, tabSelect);
                sql.append(str);
                break;
            default:
                if (caseRDB.endsWith("1")){
                    str = new StringBuffer();
                    if (stateCol.equals(StateCol.FIRST)){
                        str.append(tabSelect+"  ");
                        stateCol = StateCol.DIFFFIST;
                    }else if (stateCol.equals(StateCol.DIFFFIST)){
                        str.append(tabSelect+", ");
                    }

                    tabSelectChil = "";
                    str.append("(SELECT\r\n");
                    str.append(tabSelect + "\tT"+ (levelChild) + "." + rowObj.getColName() + " AS ["+ rowObj.getColTag() + "/d]");
                    str.append("\r\n");
                    str.append(tabSelect+"  FROM\r\n");
                    str.append(tabSelect + "  [' + @db + '].dbo.R_D_"+ tableName.toUpperCase() + "_"+rowObj.getColName() + " T"+ (levelChild) +"\r\n");
                    str.append(tabSelect+"  WHERE\r\n");
                    str.append(tabSelect+ "  T." + getColId(tableName) + " = T" + levelChild +"."+ getColId(tableName) + "\r\n");
                    str.append(tabSelect+ "  FOR XML PATH(''''), TYPE\r\n");
                    str.append(tabSelect+") ["+rowObj.getColTag() + "]\r\n");
                    sql.append(str);
                    if (isGenCondition)
                        genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, str, tableName, rowObj, levelChild, tabSelect);
                }else{
                    if (stateCol.equals(StateCol.FIRST)){
                        sql.append(tabSelect+"  T"+ levelChild + ".");
                    }else if(stateCol.equals(StateCol.DIFFFIST) || stateCol.equals(StateCol.END)){
                        sql.append(tabSelect+", T"+ levelChild + ".");
                    }else if(stateCol.equals(StateCol.CONCAT)){
                       // sql.append(tabSelect);
                    }
                    if (tagParent.isEmpty()){
                        sql.append(rowObj.getColName() + " AS [" + rowObj.getColTag() +"]");
                    }else{
                        sql.append(rowObj.getColName() + " AS [" + tagP +"/"+ rowObj.getColTag() +"]");
                    }
                    if(stateCol.equals(StateCol.END)){
                        sql.append("\r\n");
                    }
                     if (isGenCondition)
                        genConditions(tagParent.isEmpty() ? sheetName : sheetName + "_" +tagParent, sql, tableName, rowObj, levelChild, tabSelect);
                }
                break;

        }


        return sql.toString();
    }

    public static void genConditions(String sheetName, StringBuffer sql, String tableName, RowObj rowObj, int levelChild, String tabSelect){
        if (tables.get(sheetName+"_"+tableName)!= null && rowObj.getIndex() == tables.get(sheetName+"_"+tableName).getRowObjs().size() -1){
            String parentId = levelChild == 1 ? "T."+ getColId(tableName) : "T"+ (levelChild -1)+ "." + getColId(tableName);
            String chilId = "T"+ levelChild+ "." + getColId(tableName);
            sql.append("\r\n"+tabSelect+"FROM\r\n");
            sql.append(tabSelect+"  [' + @db + '].dbo.R_D_"+tableName.toUpperCase() + " T"+ levelChild +"\r\n\r\n");
            sql.append(tabSelect+"WHERE\r\n");
            sql.append(tabSelect+"  "+ parentId + " = " + chilId+"\r\n");
          /*  if (tables.size() != 1 ){
                sql.append(tabSelect+"FOR XML PATH(''" + rowObj.getColTag()+ "''), ROOT(''r'')");
            }
            else {*/
               // sql.append(tabSelect+"FOR XML PATH(''''), ROOT(''r'')");
            sql.append(tabSelect+"FOR XML PATH(''r'')");
           // }
        }
    }

    public static void writeStoredProcedure(String nameFile, String data){
        try {
            Writer unicodeFileWriter = new OutputStreamWriter(new FileOutputStream(nameFile), "UTF-8");
            unicodeFileWriter.write(data);
            unicodeFileWriter.flush();
            unicodeFileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
