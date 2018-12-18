package com.fsryan.forsuredb.sqlitelib.diff;

class StringUtil {

    static StringBuilder cutDownBuf(StringBuilder buf, int by) {
        by = buf.length() < by ? buf.length() : by;
        return buf.delete(buf.length() - by, buf.length());
    }
}
