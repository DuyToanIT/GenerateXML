import java.util.ArrayList;
import java.util.List;

public class TableObj {
    private String tableName;
    private List<RowObj> rowObjs;

    public TableObj() {
        this.rowObjs = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<RowObj> getRowObjs() {
        return rowObjs;
    }

    public void setRowObjs(List<RowObj> rowObjs) {
        this.rowObjs = rowObjs;
    }
}
