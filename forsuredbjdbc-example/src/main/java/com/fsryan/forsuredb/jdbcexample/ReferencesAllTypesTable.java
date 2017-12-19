package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSForeignKey;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.annotations.Index;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.Retriever;

@FSTable("references_all_types")
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
            value = "composed_int",
            documentValueAccess = {"getComposedPojo", "getComposedInt"}
    )
    @Index
    int composedInt(Retriever retriever);
}
