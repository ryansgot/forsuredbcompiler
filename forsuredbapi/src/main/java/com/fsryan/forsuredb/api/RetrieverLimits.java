package com.fsryan.forsuredb.api;

public interface RetrieverLimits {
    RetrieverLimits NONE = new RetrieverLimits() {
        @Override
        public int limit() {
            return 0;
        }

        @Override
        public int offset() {
            return 0;
        }

        @Override
        public boolean fromBottom() {
            return false;
        }
    };

    /**
     * @return the limit on the number of records to return
     */
    int limit();

    /**
     * @return the offset on when to start returning records
     */
    int offset();

    /**
     * If false, the records should get returned from the beginning of the result set first.
     * If true, then the records should get returned from the end of the result set first.
     * @return true if the bottom of the result set should get returned
     */
    boolean fromBottom();
}
