package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureJdbcInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsryan.forsuredb.jdbcexample.util.MyPojoInputter;
import com.fsryan.forsuredb.jdbcexample.util.RecordModel;
import com.fsryan.forsuredb.jdbcexample.util.RecordModelInputter;
import org.beryx.textio.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.function.BiConsumer;

import static com.fsryan.forsuredb.jdbcexample.ForSure.allTypesTable;
import static com.fsryan.forsuredb.jdbcexample.ForSure.referencesAllTypesTable;


public class ExampleApp {

    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_FORMAT_STRING));

    private static class AllTypesTableData implements BiConsumer<TextIO, String> {

        private static final int PRINT_RECORDS = 1;
        private static final int INSERT_RECORD = PRINT_RECORDS + 1;
        private static final int UPSERT_RECORD = INSERT_RECORD + 1;
        private static final int UPDATE_RECORD = UPSERT_RECORD + 1;
        private static final int DELETE_RECORD = UPDATE_RECORD + 1;
        private static final int QUIT = DELETE_RECORD + 1;

        private static final int PRINT_ALL_RECORDS_ALL_TYPES = 1;
        private static final int PRINT_ALL_RECORDS_DOC_STORE = PRINT_ALL_RECORDS_ALL_TYPES + 1;
        private static final int PRINT_ALL_RECORDS_JOINED = PRINT_ALL_RECORDS_DOC_STORE + 1;
        private static final int PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID = PRINT_ALL_RECORDS_JOINED + 1;
        private static final int PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID = PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID + 1;
        private static final int PRINT_SPECIFIC_JOINED_RECORD_BY_ALL_TYPES_ID = PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID + 1;

        @Override
        public void accept(TextIO textIO, String initialData) {
            TextTerminal<?> terminal = textIO.getTextTerminal();

            int selection = 0;
            while (selection != QUIT) {
                selection = prompt(textIO);
                switch (selection) {
                    case PRINT_RECORDS:
                        printRecords(textIO);
                        break;
                    case INSERT_RECORD:
                        upsert(textIO, true);
                        break;
                    case UPSERT_RECORD:
                        upsert(textIO, false);
                        break;
                    case UPDATE_RECORD:
                        update(textIO);
                        break;
                    case DELETE_RECORD:
                        delete(textIO);
                        break;
                    case QUIT:
                        terminal.println("Thanks.");
                        break;
                    default:
                        throw new RuntimeException("Did not expect selection: " + selection);
                }
                terminal.println();
                terminal.println();
            }

            textIO.dispose();
        }

        private int prompt(TextIO textIO) {
            return textIO.newIntInputReader()
                    .withPossibleValues(PRINT_RECORDS, INSERT_RECORD, UPSERT_RECORD, UPDATE_RECORD, DELETE_RECORD, QUIT)
                    .withDefaultValue(QUIT)
                    .read(
                            "Choose from the following options",
                            PRINT_RECORDS + ". print records",
                            INSERT_RECORD + ". insert record",
                            UPSERT_RECORD + ". upsert record",
                            UPDATE_RECORD + ". update record",
                            DELETE_RECORD + ". delete record",
                            QUIT + ". quit"
                    );
        }

        private void upsert(TextIO textIO, boolean newRecord) {
            TextTerminal<?> terminal = textIO.getTextTerminal();
            terminal.println("Upserting");
            terminal.println();

            switch (textIO.newIntInputReader()
                    .withPossibleValues(1, 2)
                    .read("What kind of record would you like to insert?",
                            "1. all_types record",
                            "2. pojo doc store record"
                    )) {
                case 1:
                    upsertAllTypes(textIO, newRecord);
                    break;
                case 2:
                    upsertDocStore(textIO, newRecord);
                    break;
                default:
                    throw new RuntimeException("unexpected selection");
            }
        }

        private void upsertAllTypes(TextIO textIO, boolean newRecord) {
            RecordModel recordModel = RecordModelInputter.withRandomSuggestions(textIO).createRecord();

            AllTypesTableSetter setter = newRecord
                    ? allTypesTable().set()
                    : allTypesTable().find()
                    .byId(textIO.newLongInputReader()
                            .read("ID of the record you want to upsert"))
                    .then()
                    .set();
            summarizeSave(textIO, saveRecordModel(setter, recordModel));
        }

        private void upsertDocStore(TextIO textIO, boolean newRecord) {
            MyPojo myPojo = MyPojoInputter.withRandomSuggestions(textIO).createRecord();

            long allTypesId = textIO.newLongInputReader()
                    .read(newRecord ? "all_types_id" : "ID of the record you want to upsert");

            ReferencesAllTypesTableSetter setter = newRecord
                    ? referencesAllTypesTable().set().allTypesId(allTypesId)
                    : referencesAllTypesTable().find()
                        .byId(allTypesId)
                    .then()
                    .set();

            summarizeSave(textIO, setter.object(myPojo).save());
        }
        /*
                private static final int PRINT_ALL_RECORDS_ALL_TYPES = 1;
        private static final int PRINT_ALL_RECORDS_DOC_STORE = PRINT_ALL_RECORDS_ALL_TYPES + 1;
        private static final int PRINT_ALL_RECORDS_JOINED = PRINT_ALL_RECORDS_DOC_STORE + 1;
        private static final int PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID = PRINT_ALL_RECORDS_JOINED + 1;
        private static final int PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID = PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID + 1;
        private static final int PRINT_SPECIFIC_JOINED_RECORD_BY_ALL_TYPES_ID = PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID + 1;
         */

        private void printRecords(TextIO textIO) {
            int selection = textIO.newIntInputReader()
                    .withDefaultValue(1)
                    .withPossibleValues(
                            PRINT_ALL_RECORDS_ALL_TYPES,
                            PRINT_ALL_RECORDS_DOC_STORE,
                            PRINT_ALL_RECORDS_JOINED,
                            PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID,
                            PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID,
                            PRINT_SPECIFIC_JOINED_RECORD_BY_ALL_TYPES_ID
                    ).read(
                            "How would you like to print records?",
                            PRINT_ALL_RECORDS_ALL_TYPES + ". Print all records of all_types table",
                            PRINT_ALL_RECORDS_DOC_STORE + ". Print all records of doc store table",
                            PRINT_ALL_RECORDS_JOINED + ". Print the joined records (the doc store table references the all_types table)",
                            PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID + ". Print specific record of all_types table",
                            PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID + ". Print specific record of doc store table",
                            PRINT_SPECIFIC_JOINED_RECORD_BY_ALL_TYPES_ID + ". Print specific joined record (the doc store table references the all_types table)"
                    );

            switch (selection) {
                case PRINT_ALL_RECORDS_ALL_TYPES:
                    printAllRecords(textIO.getTextTerminal(), true, false);
                    break;
                case PRINT_ALL_RECORDS_DOC_STORE:
                    printAllRecords(textIO.getTextTerminal(), false, false);
                    break;
                case PRINT_ALL_RECORDS_JOINED:
                    printAllRecords(textIO.getTextTerminal(), true, true);
                    break;
                case PRINT_SPECIFIC_RECORD_ALL_TYPES_BY_ID:
                    printRecordWithId(textIO.getTextTerminal(), textIO.newLongInputReader().read("id"), true, false);
                    break;
                case PRINT_SPECIFIC_RECORD_DOC_STORE_BY_ID:
                    printRecordWithId(textIO.getTextTerminal(), textIO.newLongInputReader().read("id"), false, false);
                    break;
                case PRINT_SPECIFIC_JOINED_RECORD_BY_ALL_TYPES_ID:
                    printRecordWithId(textIO.getTextTerminal(), textIO.newLongInputReader().read("id"), true, true);
                    break;
            }
        }

        private void printAllRecords(TextTerminal<?> textTerminal, boolean allTypes, boolean joined) {
            textTerminal.println("Fetching all records from database...");
            if (joined) {
                try (Retriever r = referencesAllTypesTable().joinAllTypesTable(FSJoin.Type.INNER).then().get()) {
                    printRecordsForRetriever(textTerminal, r, true, true);
                }
                return;
            }

            if (allTypes) {
                try (Retriever r = allTypesTable().get()) {
                    printRecordsForRetriever(textTerminal, r, true, false);
                }
                return;
            }

            try (Retriever r = referencesAllTypesTable().get()) {
                printRecordsForRetriever(textTerminal, r, false, false);
            }
        }

        private void printRecordWithId(TextTerminal<?> textTerminal, long id, boolean allTypes, boolean joined) {
            String message = joined
                    ? "Fetching joined records from all_types table and doc store table by all_types._id = " + id
                    : allTypes
                    ? "Fetching records from all_types table with _id = " + id
                    : "Fetching records from doc store table with _id = " + id;
            textTerminal.println(message);

            if (joined) {
                try (Retriever r = allTypesTable()
                        .joinReferencesAllTypesTable(FSJoin.Type.INNER)
                        .then()
                        .find()
                            .byId(id)
                        .then()
                        .get()) {
                    printRecordsForRetriever(textTerminal, r, true, true);
                }
                return;
            }
            if (allTypes) {
                try (Retriever r = allTypesTable()
                        .find()
                            .byId(id)
                        .then()
                        .get()) {
                    printRecordsForRetriever(textTerminal, r, true, false);
                }
                return;
            }
            try (Retriever r = referencesAllTypesTable()
                    .find()
                        .byId(id)
                    .then()
                    .get()) {
                printRecordsForRetriever(textTerminal, r, false, false);
            }
        }

        private void update(TextIO textIO) {
            RecordModel recordModel = RecordModelInputter.withRandomSuggestions(textIO).createRecord();
            AllTypesTableSetter setter = allTypesTable()
                    .find()
                        .byId(textIO.newLongInputReader().read("record id"))
                    .then()
                    .set();
            summarizeSave(textIO, saveRecordModel(setter, recordModel));
        }

        private SaveResult<DirectLocator> saveRecordModel(AllTypesTableSetter setter, RecordModel recordModel) {
            return setter.bigDecimalColumn(recordModel.getBigDecimalColumn())
                    .bigIntegerColumn(recordModel.getBigIntegerColumn())
                    .byteArrayColumn(recordModel.getByteArrayColumn())
                    .dateColumn(recordModel.getDateColumn())
                    .doubleColumn(recordModel.getDoubleColumn())
                    .doubleWrapperColumn(recordModel.getDoubleWrapperColumn())
                    .floatColumn(recordModel.getFloatColumn())
                    .floatWrapperColumn(recordModel.getFloatWrapperColumn())
                    .intColumn(recordModel.getIntColumn())
                    .integerWrapperColumn(recordModel.getIntegerWrapperColumn())
                    .longColumn(recordModel.getLongColumn())
                    .longWrapperColumn(recordModel.getLongWrapperColumn())
                    .stringColumn(recordModel.getStringColumn())
                    .save();
        }

        private void delete(TextIO textIO) {
            TextTerminal<?> terminal = textIO.getTextTerminal();
            printDivider(terminal);
            terminal.println("Deleting a record");

            boolean hard = textIO.newBooleanInputReader().read("hard delete?");
            if (!hard) {
                summarizeSave(textIO, allTypesTable()
                        .find()
                            .byId(textIO.newLongInputReader().read("record id"))
                        .then()
                        .set()
                        .softDelete());
            } else {
                int hardDeleted = allTypesTable()
                        .find()
                            .byId(textIO.newLongInputReader().read("record id"))
                        .then()
                        .set()
                        .hardDelete();
                terminal.println(hardDeleted == 1 ? "Deleted" : "Failed");
                printDivider(terminal);
            }
        }

        private void printRecordsForRetriever(TextTerminal<?> textTerminal, Retriever r, boolean allTypesTable, boolean joined) {
            boolean recordFound = false;

            while (r.moveToNext()) {
                recordFound = true;

                printDivider(textTerminal);

                StringBuilder out = new StringBuilder();
                if (allTypesTable) {
                    RecordModel model = RecordModel.fromRetriever(r);
                    out.append(model.toString());
                } else {
                    MyPojo myPojo = referencesAllTypesTable().getApi().get(r);
                    out.append(myPojo.toString());
                }
                if (joined) {
                    out.append("\n**** JOINED ****\n");
                    if (allTypesTable) {
                        MyPojo myPojo = referencesAllTypesTable().getApi().get(r);
                        out.append(myPojo.toString());
                    } else {
                        RecordModel model = RecordModel.fromRetriever(r);
                        out.append(model.toString());
                    }
                }
                textTerminal.println(out.toString());
            }
            if (!recordFound) {
                textTerminal.println("No records found");
            }
        }

        private void summarizeSave(TextIO textIO, SaveResult<DirectLocator> result) {
            TextTerminal<?> terminal = textIO.getTextTerminal();
            printDivider(terminal);

            String inserted = result.inserted() == null ? "" : "DirectLocator{table=" + result.inserted().table + ",id=" +result.inserted().id +"}";
            terminal.print("SaveResult{inserted=" + inserted + ",rowsAffected=" + result.rowsAffected() + ",exception=" +result.exception() + "}");

        }
    }


    public static void main(String[] args) {
        FSDBHelper.initDebugSQLite(
                "jdbc:sqlite:example.db",
                null,
                TableGenerator.generate(),
                new FSDbInfoGsonSerializer()
        );
        ForSure.init(ForSureJdbcInfoFactory.inst());

        TextIO textIO = TextIoFactory.getTextIO();
        new AllTypesTableData().accept(textIO, null);
    }

    private static void printDivider(TextTerminal<?> terminal) {
        terminal.println("     -----     ");
    }
}
