import java.util.List;

public class RowObj {

    private int index;
    private String colName;
    private String colTag;
    private String colNameJP;
    private String colType;
    private String caseGenerate;

    public RowObj() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColTag() {
        return colTag;
    }

    public void setColTag(String colTag) {
        this.colTag = colTag;
    }

    public String getColNameJP() {
        return colNameJP;
    }

    public void setColNameJP(String colNameJP) {
        this.colNameJP = colNameJP;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public String getCaseGenerate() {
        return caseGenerate;
    }

    public void setCaseGenerate(String caseGenerate) {
        this.caseGenerate = caseGenerate;
    }
}
