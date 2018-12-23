package helpers;

import java.io.Reader;
import java.util.List;
import java.util.Vector;

public class CSVHelper {
    public static List<String> parseLine(Reader r) throws Exception {
        int ch = r.read();
        while (ch == '\n') {
            ch = r.read();
        }
        if (ch < 0) {
            return null;
        }
        Vector<String> store = new Vector<>();
        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean started = false;
        while (ch >= 0) {
            if (inQuotes) {
                started = true;
                if (ch == '\"') {
                    inQuotes = false;
                } else {
                    curVal.append((char) ch);
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                    if (started) {
                        curVal.append('\"');
                    }
                } else if (ch == ',') {
                    store.add(curVal.toString());
                    curVal = new StringBuffer();
                    started = false;
                } else if (ch == '\r') {
                    //ignore LF characters
                } else if (ch == '\n') {
                    //end of line, break
                    break;
                } else {
                    curVal.append((char) ch);
                }
            }
            ch = r.read();
        }
        store.add(curVal.toString());
        return store;
    }
}
