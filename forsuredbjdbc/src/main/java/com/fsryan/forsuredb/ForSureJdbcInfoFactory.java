package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.ForSureInfoFactory;
import com.fsryan.forsuredb.api.TypedRecordContainer;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsryan.forsuredb.queryable.JdbcQueryable;

import java.util.List;

public class ForSureJdbcInfoFactory implements ForSureInfoFactory<DirectLocator, TypedRecordContainer> {

    private static final ForSureJdbcInfoFactory instance = new ForSureJdbcInfoFactory();

    private ForSureJdbcInfoFactory() {}

    public static ForSureJdbcInfoFactory inst() {
        return instance;
    }

    @Override
    public FSQueryable<DirectLocator, TypedRecordContainer> createQueryable(DirectLocator resource) {
        return new JdbcQueryable(resource);
    }

    @Override
    public TypedRecordContainer createRecordContainer() {
        return new TypedRecordContainer();
    }

    @Override
    public DirectLocator tableResource(String tableName) {
        return new DirectLocator(tableName);
    }

    @Override
    public DirectLocator locatorFor(String tableName, long id) {
        return new DirectLocator(tableName, id);
    }

    @Override
    public DirectLocator locatorWithJoins(DirectLocator locator, List<FSJoin> joins) {
        // TODO: locator with joins doesn't make sense because we're sending the joins in anyway in the call to the Queryable
        locator.addJoins(joins);
        return locator;
    }

    @Override
    public String tableName(DirectLocator locator) {
        return locator.table;
    }
}
