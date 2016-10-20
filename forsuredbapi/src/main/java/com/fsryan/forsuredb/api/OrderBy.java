package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.Sql;

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

    public static final int ORDER_ASC = 0;
    public static final int ORDER_DESC = -1;

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
        return Sql.generator().combineOrderByExpressions(orderByList);
    }

    protected void appendOrder(String columnName, int order) {
        orderByList.add(order == ORDER_ASC ? Sql.generator().orderByAsc(tableName, columnName)
                : Sql.generator().orderByDesc(tableName, columnName));
    }
}
