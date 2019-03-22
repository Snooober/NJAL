package helpers;

import java.util.ArrayList;
import java.util.List;

public class ListMessageBuilder {
    List<List<String>> columnList;
    List<String> fullMsgList;

    public ListMessageBuilder() {
        columnList = new ArrayList<>();
        fullMsgList = new ArrayList<>();
    }

    public ListMessageBuilder(List<List<String>> columnList) {
        this.columnList = columnList;
        fullMsgList = new ArrayList<>();
    }

    public void addColumn(List<String> column) {
        columnList.add(column);
    }

    public void addColumn(List<String> column, )
}
