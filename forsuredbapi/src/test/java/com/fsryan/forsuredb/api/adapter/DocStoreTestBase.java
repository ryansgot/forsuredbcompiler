package com.fsryan.forsuredb.api.adapter;

import lombok.AccessLevel;

import java.math.BigDecimal;
import java.util.Date;

@lombok.Data
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder")
/*package*/ class DocStoreTestBase {
    private BigDecimal bigDecimalColumn;
    private boolean booleanColumn;
    private Boolean booleanWrapperColumn;
    private byte[] byteArrayColumn;
    private float floatColumn;
    private Float floatWrapperColumn;
    private double doubleColumn;
    private Double doubleWrapperColumn;
    private Date dateColumn;
    private int intColumn;
    private Integer integerWrapperColumn;
    private long longColumn;
    private Long longWrapperColumn;
    private String stringColumn;

    @lombok.Data
    @lombok.ToString(callSuper = true)
    public static class Extension extends DocStoreTestBase {

        private String extraStringColumn;

        public Extension(DocStoreTestBase base, String extraStringColumn) {
            this(base.getBigDecimalColumn(),
                    base.isBooleanColumn(),
                    base.getBooleanWrapperColumn(),
                    base.getByteArrayColumn(),
                    base.getFloatColumn(),
                    base.getFloatWrapperColumn(),
                    base.getDoubleColumn(),
                    base.getDoubleWrapperColumn(),
                    base.getDateColumn(),
                    base.getIntColumn(),
                    base.getIntegerWrapperColumn(),
                    base.getLongColumn(),
                    base.getLongWrapperColumn(),
                    base.getStringColumn(),
                    extraStringColumn);
        }

        public Extension(BigDecimal bigDecimalColumn, boolean booleanColumn, Boolean booleanWrapperColumn, byte[] byteArrayColumn, float floatColumn, Float floatWrapperColumn, double doubleColumn, Double doubleWrapperColumn, Date dateColumn, int intColumn, Integer integerWrapperColumn, long longColumn, Long longWrapperColumn, String stringColumn, String extraStringColumn) {
            super(bigDecimalColumn, booleanColumn, booleanWrapperColumn, byteArrayColumn, floatColumn, floatWrapperColumn, doubleColumn, doubleWrapperColumn, dateColumn, intColumn, integerWrapperColumn, longColumn, longWrapperColumn, stringColumn);
            this.extraStringColumn = extraStringColumn;
        }
    }
}
