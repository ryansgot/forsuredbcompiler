package com.fsryan.forsuredb.api;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *   <i>WARNING:</i> order-by methods on {@link java.math.BigDecimal} columns may not
 *   sort based upon numeric value, depending upon DBMS. For example, current known
 *   SQLite fsryan integrations store BigDecimal values as string columns, and
 *   therefore, will sort as strings rather than numbers. This is currently a limitation.
 * </p>
 */
public abstract class OrderBy {

    public enum Order {
        ASC, DESC
    }

    private final List<String> orderByList;
    private final String tableName;

    public OrderBy(String tableName) {
        this.tableName = tableName;
        orderByList = new ArrayList<>();
    }

    /**
     * @return the SQL string for the order by portion of the query
     */
    public String getOrderByString() {
        // kind of a trick, but it so happens that the toString method for ArrayList almost works
        // for the ORDER BY syntax.
        return orderByList.size() == 0 ? "" : orderByList.toString().replaceAll("(\\[|\\])", "");
    }

    protected void appendOrder(String columnName, Order order) {
        if (order != null) {
            orderByList.add(tableName + "." + columnName + " " + order.name());
        }
    }
}
