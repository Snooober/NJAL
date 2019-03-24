package helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

//TODO warn about have ColumnType.DEFAULT after ColumnType.HYPERLINK
//TODO check that columns added are the same size

public class DiscordTableBuilder {
    private List<String> fullMsgList;
    private List<ColumnElement> columnList;
    private String title = " ";

    public DiscordTableBuilder() {
        columnList = new ArrayList<>();
        fullMsgList = new ArrayList<>();
    }

    public DiscordTableBuilder(List<List<String>> columnList) {
        this.columnList = new ArrayList<>();
        for (List<String> entries : columnList
        ) {
            this.columnList.add(new ColumnElement(entries));
        }
        fullMsgList = new ArrayList<>();
    }

    public DiscordTableBuilder(List<List<String>> columnList, ColumnType columnType) {
        this.columnList = new ArrayList<>();
        for (List<String> entries : columnList
        ) {
            this.columnList.add(new ColumnElement(entries, columnType));
        }
        fullMsgList = new ArrayList<>();
    }

    public void addColumn(List<String> entries) {
        columnList.add(new ColumnElement(entries));
    }

    public void addColumn(List<String> entries, ColumnType columnType) {
        columnList.add(new ColumnElement(entries, columnType));
    }

    public void addTitle(String title) {
        this.title = title;
    }

    public List<String> build() {
        //make entries in each column the same size
        equalColSize();

        //titlebar
        String message = "```" + title + "```";

        int numRows = columnList.get(0).entries.size();
        int rowIndex = 0;
        String potentialMsg = "";
        while (rowIndex < numRows) {
            //for each row, iterate through columns and add entries to potentialMsg
            Iterator<ColumnElement> columnsListIt = columnList.iterator();
            while (columnsListIt.hasNext()) {
                ColumnElement columnElement = columnsListIt.next();
                switch (columnElement.columnType) {
                    case HYPERLINK:
                        //rowIndex 0 is title row and gets same treatment as ColumnType.DEFAULT
                        if (rowIndex != 0) {
                            potentialMsg = potentialMsg.concat("<" + columnElement.entries.get(rowIndex) + "> ");
                            break;
                        }
                    case DEFAULT:
                        potentialMsg = potentialMsg.concat("`" + columnElement.entries.get(rowIndex) + "` ");
                        break;
                }
            }
            //Remove last trailing space
            potentialMsg = potentialMsg.trim();

            //max char limit is 2000, so if concatenating the potentialMsg to message will be less then 1996, go ahead and concat.
            //if not, add current message to array and then make a new message for the bot to send
            if ((message.length() + potentialMsg.length()) <= 1996) {
                message = message.concat(potentialMsg);
            } else {
                fullMsgList.add(message);
                message = potentialMsg;
            }

            //end of row
            message = message.concat("\n");
            potentialMsg = "";
            rowIndex++;
        }
        fullMsgList.add(message);
        return fullMsgList;
    }

    private void equalColSize() {
        ListIterator<ColumnElement> columnListIt = columnList.listIterator();
        while (columnListIt.hasNext()) {
            ColumnElement columnElement = columnListIt.next();
            if (columnElement.columnType.equals(ColumnType.DEFAULT)) {
                equalStrLength(columnElement.entries);
            }
        }
    }

    private void equalStrLength(List<String> entries) {
        int maxSize = 0;
        ListIterator<String> entriesIt = entries.listIterator();
        while (entriesIt.hasNext()) {
            String entry = entriesIt.next();
            if (entry.length() > maxSize) {
                maxSize = entry.length();
            }
        }

        entriesIt = entries.listIterator();
        while (entriesIt.hasNext()) {
            String entry = entriesIt.next();
            int neededSpaces = maxSize - entry.length();

            for (int i = 0; i < neededSpaces; i++) {
                entry = entry.concat(" ");
            }
            //add zero-width space so that the code block won't trim trailing spaces in discord
            entry = entry.concat("\u200B");
            entriesIt.set(entry);
        }
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
