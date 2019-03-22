package helpers;

import java.util.ArrayList;
import java.util.List;

public class ListMessageBuilder {
    private List<String> fullMsgList;
    private List<ColumnElement> columnList;

    public ListMessageBuilder() {
        columnList = new ArrayList<>();
        fullMsgList = new ArrayList<>();
    }

    public ListMessageBuilder(List<List<String>> columnList) {
        for (:
             ){

        }


    }

    public void addColumn(List<String> column) {
        columnList.add(new ColumnElement(column));
    }

    public void addColumn(List<String> column, ColumnType columnType) {
        columnList.add(new ColumnElement(column, columnType));
    }

    private class ColumnElement {
        private List<String> entries;
        private ColumnType columnType;

        private ColumnElement(List<String> entries) {
            this.entries = entries;
            this.columnType = ColumnType.DEFAULT;
        }

        private ColumnElement(List<String> entries, ColumnType columnType) {
            this.entries = entries;
            this.columnType = columnType;
        }
    }
}
