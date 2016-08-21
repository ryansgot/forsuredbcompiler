package com.fsryan.forsuredb.api.adapter;

import java.math.BigDecimal;
import java.util.Date;

@lombok.Data
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder")
/*package*/ class DocStoreTestBase {
    private BigDecimal bigDecimalColumn;
    private boolean booleanColumn;
    private Boolean booleanWrapperColumn;
    private byte[] byteArrayColumn;
    private double doubleColumn;
    private Double doubleWrapperColumn;
    private Date dateColumn;
    private int intColumn;
    private Integer integerWrapperColumn;
    private long longColumn;
    private Long longWrapperColumn;
    private String stringColumn;
}
