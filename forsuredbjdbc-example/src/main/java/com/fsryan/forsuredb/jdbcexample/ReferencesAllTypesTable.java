package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.Retriever;

@FSTable("references_all_types")
@FSStaticData("references_all_types_static_data.xml")
public interface ReferencesAllTypesTable extends FSDocStoreGetApi<MyPojo> {
    Class BASE_CLASS = MyPojo.class;
    @FSColumn("all_types_id")
    @FSForeignKey(
            apiClass = AllTypesTable.class,     // <-- allows for code generation of resolver to account for join
            columnName = "_id",                 // <-- _id is a built-in primary key (unless you use a composite primary key)
            updateAction = "CASCADE",           // <-- what to do when the _id column is updated
            deleteAction = "CASCADE"            // <-- what to do when the record pointed to is deleted
    )
    long allTypesId(Retriever retriever);

    @FSColumn(
            value = "composed_int",                                     // <-- the name of the column
            documentValueAccess = {"getComposedPojo", "getComposedInt"} // <-- the sequence of methods to call in a chain to get the value
    )
    @Index
    int composedInt(Retriever retriever);
}
