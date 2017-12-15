package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureJdbcInfoFactory;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsryan.forsuredb.util.MyPojoInputter;
import com.fsryan.forsuredb.util.RecordModel;
import com.fsryan.forsuredb.util.RecordModelInputter;
import org.beryx.textio.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.function.BiConsumer;


public class ExampleApp {

    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    private static class AllTypesTableData implements BiConsumer<TextIO, String> {

        private static final int PRINT_RECORDS = 1;
        private static final int INSERT_RECORD = PRINT_RECORDS + 1;
        private static final int UPSERT_RECORD = INSERT_RECORD + 1;
        private static final int UPDATE_RECORD = UPSERT_RECORD + 1;
        private static final int DELETE_RECORD = UPDATE_RECORD + 1;
        private static final int QUIT = DELETE_RECORD + 1;

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
            }
        }

        private void upsertAllTypes(TextIO textIO, boolean newRecord) {
            RecordModel recordModel = RecordModelInputter.withRandomSuggestions(textIO).createRecord();

            AllTypesTableSetter setter = newRecord
                    ? ForSure.allTypesTable().set()
                    : ForSure.allTypesTable().find()
                    .byId(textIO.newLongInputReader()
                            .read("ID of the record you want to upsert"))
                    .then()
                    .set();

            SaveResult<DirectLocator> result = setter.bigDecimalColumn(recordModel.getBigDecimalColumn())
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
            summarizeSave(textIO, result);
        }

        private void upsertDocStore(TextIO textIO, boolean newRecord) {
            MyPojo myPojo = MyPojoInputter.withRandomSuggestions(textIO).createRecord();

            long allTypesId = textIO.newLongInputReader()
                    .read(newRecord ? "all_types_id" : "ID of the record you want to upsert");

            ReferencesAllTypesTableSetter setter = newRecord
                    ? ForSure.referencesAllTypesTable().set().allTypesId(allTypesId)
                    : ForSure.referencesAllTypesTable().find()
                        .byId(allTypesId)
                    .then()
                    .set();

            summarizeSave(textIO, setter.object(myPojo).save());
        }

        private void printRecords(TextIO textIO) {
            int selection = textIO.newIntInputReader()
                    .withDefaultValue(1)
                    .withPossibleValues(1, 2, 3)
                    .read(
                            "How would you like to return records?",
                            "1. Print all records",
                            "2. Print Record with id",
                            "3. Enter search criteria"
                    );

            switch (selection) {
                case 1:
                    printAllRecords(textIO.getTextTerminal());
                    break;
                case 2:
                    printRecordWithId(textIO.getTextTerminal(), textIO.newLongInputReader().read("id"));
                    break;
                case 3:
                    RecordModel recordModel = RecordModelInputter.withoutSuggestions(textIO).createRecord();
                    printAllRecordsMatchingCriteria(textIO.getTextTerminal(), recordModel);
            }
        }

        private void printAllRecords(TextTerminal<?> textTerminal) {
            textTerminal.println("Fetching all records from database...");
            try (Retriever r = ForSure.allTypesTable().get()) {
                printRecordsForRetriever(textTerminal, r);
            }
        }

        private void printRecordWithId(TextTerminal<?> textTerminal, long id) {
            textTerminal.println("Fetching record " + id + " from database...");
            try (Retriever r = ForSure.allTypesTable()
                    .find()
                        .byId(id)
                    .then()
                    .get()) {
                printRecordsForRetriever(textTerminal, r);
            }
        }

        private void update(TextIO textIO) {
            RecordModel recordModel = RecordModelInputter.withRandomSuggestions(textIO).createRecord();
            SaveResult<DirectLocator> result = ForSure.allTypesTable()
                    .find()
                        .byId(textIO.newLongInputReader().read("record id"))
                    .then()
                    .set()
                        .bigDecimalColumn(recordModel.getBigDecimalColumn())
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
            summarizeSave(textIO, result);
        }

        private void delete(TextIO textIO) {
            TextTerminal<?> terminal = textIO.getTextTerminal();
            printDivider(terminal);
            terminal.println("Deleting a record");

            boolean hard = textIO.newBooleanInputReader().read("hard delete?");
            if (!hard) {
                summarizeSave(textIO, ForSure.allTypesTable()
                        .find()
                            .byId(textIO.newLongInputReader().read("record id"))
                        .then()
                        .set()
                        .softDelete());
            } else {
                int hardDeleted = ForSure.allTypesTable()
                        .find()
                            .byId(textIO.newLongInputReader().read("record id"))
                        .then()
                        .set()
                        .hardDelete();
                terminal.println(hardDeleted == 1 ? "Deleted" : "Failed");
                printDivider(terminal);
            }
        }

        private void printAllRecordsMatchingCriteria(TextTerminal<?> textTerminal, RecordModel record) {
            // TODO
        }

        private void printRecordsForRetriever(TextTerminal<?> textTerminal, Retriever r) {
            boolean recordFound = false;
            while (r.moveToNext()) {
                recordFound = true;
                printDivider(textTerminal);
                textTerminal.println(RecordModel.fromRetriever(r).toString());
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
