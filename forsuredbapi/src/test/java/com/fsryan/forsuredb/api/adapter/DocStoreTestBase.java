package com.fsryan.forsuredb.api.adapter;



import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@AutoValue
abstract class DocStoreTestBase implements Serializable {

    @AutoValue.Builder
    static abstract class Builder {
        public abstract Builder bigIntegerColumn(BigInteger bigIntegerColumn);
        public abstract Builder bigDecimalColumn(BigDecimal bigDecimalColumn);
        public abstract Builder booleanColumn(boolean booleanColumn);
        public abstract Builder booleanWrapperColumn(Boolean booleanWrapperColumn);
        public abstract Builder byteArrayColumn(byte[] byteArrayColumn);
        public abstract Builder floatColumn(float floatColumn);
        public abstract Builder floatWrapperColumn(Float floatWrapperColumn);
        public abstract Builder doubleColumn(double doubleColumn);
        public abstract Builder doubleWrapperColumn(Double doubleWrapperColumn);
        public abstract Builder dateColumn(Date dateColumn);
        public abstract Builder intColumn(int intColumn);
        public abstract Builder integerWrapperColumn(Integer integerWrapperColumn);
        public abstract Builder longColumn(long longColumn);
        public abstract Builder longWrapperColumn(Long longWrapperColumn);
        public abstract Builder stringColumn(String stringColumn);
        public abstract DocStoreTestBase build();
    }

    public static Builder builder() {
        return new AutoValue_DocStoreTestBase.Builder();
    }

    public abstract BigInteger bigIntegerColumn();
    public abstract BigDecimal bigDecimalColumn();
    public abstract boolean booleanColumn();
    public abstract Boolean booleanWrapperColumn();
    @SuppressWarnings("mutable") public abstract byte[] byteArrayColumn();
    public abstract float floatColumn();
    public abstract Float floatWrapperColumn();
    public abstract double doubleColumn();
    public abstract Double doubleWrapperColumn();
    public abstract Date dateColumn();
    public abstract int intColumn();
    public abstract Integer integerWrapperColumn();
    public abstract long longColumn();
    public abstract Long longWrapperColumn();
    public abstract String stringColumn();

    public static class Extension extends DocStoreTestBase {

        private DocStoreTestBase delegate;
        private String extraStringColumn;

        public Extension(DocStoreTestBase base, String extraStringColumn) {
            delegate = base;
            this.extraStringColumn = extraStringColumn;
        }

        @Override
        public BigInteger bigIntegerColumn() {
            return delegate.bigIntegerColumn();
        }

        @Override
        public BigDecimal bigDecimalColumn() {
            return delegate.bigDecimalColumn();
        }

        @Override
        public boolean booleanColumn() {
            return delegate.booleanColumn();
        }

        @Override
        public Boolean booleanWrapperColumn() {
            return delegate.booleanWrapperColumn();
        }

        @Override
        public byte[] byteArrayColumn() {
            return delegate.byteArrayColumn();
        }

        @Override
        public float floatColumn() {
            return delegate.floatColumn();
        }

        @Override
        public Float floatWrapperColumn() {
            return delegate.floatWrapperColumn();
        }

        @Override
        public double doubleColumn() {
            return delegate.doubleColumn();
        }

        @Override
        public Double doubleWrapperColumn() {
            return delegate.doubleWrapperColumn();
        }

        @Override
        public Date dateColumn() {
            return delegate.dateColumn();
        }

        @Override
        public int intColumn() {
            return delegate.intColumn();
        }

        @Override
        public Integer integerWrapperColumn() {
            return delegate.integerWrapperColumn();
        }

        @Override
        public long longColumn() {
            return delegate.longColumn();
        }

        @Override
        public Long longWrapperColumn() {
            return delegate.longWrapperColumn();
        }

        @Override
        public String stringColumn() {
            return delegate.stringColumn();
        }

        public String getExtraStringColumn() {
            return extraStringColumn;
        }
    }
}
