package com.fsryan.forsuredb.api;

public interface Limits {
    Limits NONE = new Limits() {
        @Override
        public int count() {
            return 0;
        }

        @Override
        public int offset() {
            return 0;
        }

        @Override
        public boolean isBottom() {
            return false;
        }
    };

    /**
     * @return the max number of records to match
     */
    int count();

    /**
     * @return the the number of records matching to skip
     */
    int offset();

    /**
     * If false, the records will get matched starting at the first of the result set and the
     * offset is from the first
     * If true, then the records will be matched starting at the last of the result set
     * and the offset is from the last
     * @return true if last of result set should be matched
     */
    boolean isBottom();
}
