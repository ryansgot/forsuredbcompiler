package com.fsryan.forsuredb.util;

import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.jdbcexample.AllTypesTable;
import com.fsryan.forsuredb.jdbcexample.ForSure;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static com.fsryan.forsuredb.jdbcexample.ExampleApp.DATE_FORMAT;

public class RecordModel {

    private static AllTypesTable api = ForSure.allTypesTable().getApi();

    private Long id;
    private Boolean deleted;
    private Date created;
    private Date modified;
    private Integer intColumn;
    private Integer integerWrapperColumn;
    private Long longColumn;
    private Long longWrapperColumn;
    private Float floatColumn;
    private Float floatWrapperColumn;
    private Double doubleColumn;
    private Double doubleWrapperColumn;
    private byte[] byteArrayColumn;
    private String stringColumn;
    private BigInteger bigIntegerColumn;
    private BigDecimal bigDecimalColumn;
    private Date dateColumn;

    public static RecordModel fromRetriever(Retriever r) {
        RecordModel ret = new RecordModel();
        ret.id = api.id(r);
        ret.created = api.created(r);
        ret.deleted = api.deleted(r);
        ret.modified = api.modified(r);
        ret.intColumn = api.intColumn(r);
        ret.integerWrapperColumn = api.integerWrapperColumn(r);
        ret.longColumn = api.longColumn(r);
        ret.longWrapperColumn = api.longWrapperColumn(r);
        ret.floatColumn = api.floatColumn(r);
        ret.floatWrapperColumn = api.floatWrapperColumn(r);
        ret.doubleColumn = api.doubleColumn(r);
        ret.doubleWrapperColumn = api.doubleWrapperColumn(r);
        ret.byteArrayColumn = api.byteArrayColumn(r);
        ret.stringColumn = api.stringColumn(r);
        ret.bigIntegerColumn = api.bigIntegerColumn(r);
        ret.bigDecimalColumn = api.bigDecimalColumn(r);
        ret.dateColumn = api.dateColumn(r);
        return ret;
    }

    public Long getId() {
        return id;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }

    public Integer getIntColumn() {
        return intColumn;
    }

    public void setIntColumn(Integer intColumn) {
        this.intColumn = intColumn;
    }

    public Integer getIntegerWrapperColumn() {
        return integerWrapperColumn;
    }

    public void setIntegerWrapperColumn(Integer integerWrapperColumn) {
        this.integerWrapperColumn = integerWrapperColumn;
    }

    public Long getLongColumn() {
        return longColumn;
    }

    public void setLongColumn(Long longColumn) {
        this.longColumn = longColumn;
    }

    public Long getLongWrapperColumn() {
        return longWrapperColumn;
    }

    public void setLongWrapperColumn(Long longWrapperColumn) {
        this.longWrapperColumn = longWrapperColumn;
    }

    public Float getFloatColumn() {
        return floatColumn;
    }

    public void setFloatColumn(Float floatColumn) {
        this.floatColumn = floatColumn;
    }

    public Float getFloatWrapperColumn() {
        return floatWrapperColumn;
    }

    public void setFloatWrapperColumn(Float floatWrapperColumn) {
        this.floatWrapperColumn = floatWrapperColumn;
    }

    public Double getDoubleColumn() {
        return doubleColumn;
    }

    public void setDoubleColumn(Double doubleColumn) {
        this.doubleColumn = doubleColumn;
    }

    public Double getDoubleWrapperColumn() {
        return doubleWrapperColumn;
    }

    public void setDoubleWrapperColumn(Double doubleWrapperColumn) {
        this.doubleWrapperColumn = doubleWrapperColumn;
    }

    public byte[] getByteArrayColumn() {
        return byteArrayColumn;
    }

    public void setByteArrayColumn(byte[] byteArrayColumn) {
        this.byteArrayColumn = byteArrayColumn;
    }

    public String getStringColumn() {
        return stringColumn;
    }

    public void setStringColumn(String stringColumn) {
        this.stringColumn = stringColumn;
    }

    public BigInteger getBigIntegerColumn() {
        return bigIntegerColumn;
    }

    public void setBigIntegerColumn(BigInteger bigIntegerColumn) {
        this.bigIntegerColumn = bigIntegerColumn;
    }

    public BigDecimal getBigDecimalColumn() {
        return bigDecimalColumn;
    }

    public void setBigDecimalColumn(BigDecimal bigDecimalColumn) {
        this.bigDecimalColumn = bigDecimalColumn;
    }

    public Date getDateColumn() {
        return dateColumn;
    }

    public void setDateColumn(Date dateColumn) {
        this.dateColumn = dateColumn;
    }

    @Override
    public String toString() {
        return "RecordModel{" +
                "id=" + id +
                ", deleted=" + deleted +
                ", created=" + DATE_FORMAT.format(created) +
                ", modified=" + DATE_FORMAT.format(modified) +
                ", intColumn=" + intColumn +
                ", integerWrapperColumn=" + integerWrapperColumn +
                ", longColumn=" + longColumn +
                ", longWrapperColumn=" + longWrapperColumn +
                ", floatColumn=" + floatColumn +
                ", floatWrapperColumn=" + floatWrapperColumn +
                ", doubleColumn=" + doubleColumn +
                ", doubleWrapperColumn=" + doubleWrapperColumn +
                ", byteArrayColumn=" + Arrays.toString(byteArrayColumn) +
                ", stringColumn='" + stringColumn + '\'' +
                ", bigIntegerColumn=" + bigIntegerColumn +
                ", bigDecimalColumn=" + bigDecimalColumn +
                ", dateColumn=" + DATE_FORMAT.format(dateColumn) +
                '}';
    }
}