package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureJdbcInfoFactory;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.queryable.DirectLocator;
import org.beryx.textio.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.function.BiConsumer;


public class ExampleApp {

    static final String dateFormatString = "yyyy-MM-dd HH:mm:ss.SSS";
    static final DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    private static class AllTypesTableData implements BiConsumer<TextIO, String> {

        private static final int PRINT_RECORDS = 1;
        private static final int INSERT_RECORD = PRINT_RECORDS + 1;
        private static final int UPSERT_RECORD = INSERT_RECORD + 1;
        private static final int QUIT = UPSERT_RECORD + 1;

        @Override
        public void accept(TextIO textIO, String initialData) {
            TextTerminal<?> terminal = textIO.getTextTerminal();

            int selection = 0;
            while (selection != 4) {
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
                    .withNumberedPossibleValues(PRINT_RECORDS, INSERT_RECORD, UPSERT_RECORD, QUIT)
                    .withDefaultValue(4)
                    .read(
                            "Choose from the following options",
                            PRINT_RECORDS + ". print records",
                            INSERT_RECORD + ". insert record",
                            UPSERT_RECORD + ". upsert record",
                            QUIT + ". quit"
                    );
        }

        private void upsert(TextIO textIO, boolean newRecord) {
            RecordModel recordModel = new RecordInputterWithRandomizedDefaults(textIO).createRecord();

            AllTypesTableSetter setter = newRecord
                    ? ForSure.allTypesTable().set()
                    : ForSure.allTypesTable().find()
                            .byId(textIO.newLongInputReader()
                                    .read("ID of the record you want to upsert"))
                            .then()
                            .set();

            SaveResult<DirectLocator> result = setter.bigDecimalColumn(recordModel.bigDecimalColumn)
                    .bigIntegerColumn(recordModel.bigIntegerColumn)
                    .byteArrayColumn(recordModel.byteArrayColumn)
                    .dateColumn(recordModel.dateColumn)
                    .doubleColumn(recordModel.doubleColumn)
                    .doubleWrapperColumn(recordModel.doubleWrapperColumn)
                    .floatColumn(recordModel.floatColumn)
                    .floatWrapperColumn(recordModel.floatWrapperColumn)
                    .intColumn(recordModel.intColumn)
                    .integerWrapperColumn(recordModel.integerWrapperColumn)
                    .longColumn(recordModel.longColumn)
                    .longWrapperColumn(recordModel.longWrapperColumn)
                    .stringColumn(recordModel.stringColumn)
                    .save();

            TextTerminal<?> terminal = textIO.getTextTerminal();
            printDivider(terminal);

            String inserted = result.inserted() == null ? "" : "DirectLocator{table=" + result.inserted().table + ",id=" +result.inserted().id +"}";
            terminal.print("SaveResult{inserted=" + inserted + ",rowsAffected=" + result.rowsAffected() + ",exception=" +result.exception() + "}");
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
                    printAllRecordsMatchingCriteria(textIO.getTextTerminal(), new RecordInputterWithNoDefaults(textIO).createRecord());
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

    static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static void printDivider(TextTerminal<?> terminal) {
        terminal.println("     -----     ");
    }

    private static abstract class RecordInputter {

        private final TextIO textIO;

        RecordInputter(TextIO textIO) {
            this.textIO = textIO;
        }

        final RecordModel createRecord() {
            RecordModel ret = new RecordModel();
            ret.intColumn = readIntColumn("int_column");
            ret.integerWrapperColumn = readIntColumn("integer_wrapper_column");
            ret.longColumn = readLongColumn("long_column");
            ret.longWrapperColumn = readLongColumn("long_wrapper_column");
            ret.floatColumn = readFloatColumn("float_column");
            ret.floatWrapperColumn = readFloatColumn("float_wrapper_column");
            ret.doubleColumn = readDoubleColumn("double_column");
            ret.doubleWrapperColumn = readDoubleColumn("double_wrapper_column");
            ret.byteArrayColumn = hexStringToByteArray(readStringColumn(true, "byte_array_column"));
            ret.stringColumn = readStringColumn(false, "string_column");
            ret.bigIntegerColumn = new BigInteger(Long.toString(readLongColumn("big_integer_column")));
            ret.bigDecimalColumn = new BigDecimal(Double.toString(readDoubleColumn("big_decimal_column")));
            ret.dateColumn = readDateColumn("date_column");
            return ret;
        }

        abstract Integer intSuggestion(String columnName);
        abstract Long longSuggestion(String columnName);
        abstract Float floatSuggestion(String columnName);
        abstract Double doubleSuggestion(String columnName);
        abstract String stringSuggestion(boolean limitedToHex, String columnName);
        abstract Date dateSuggestion(String columnName);

        private Integer readIntColumn(String columnName) {
            return textIO.newIntInputReader()
                    .withDefaultValue(intSuggestion(columnName))
                    .read(columnName);
        }

        private Long readLongColumn(String columnName) {
            return textIO.newLongInputReader()
                    .withDefaultValue(longSuggestion(columnName))
                    .read(columnName);
        }

        private Float readFloatColumn(String columnName) {
            return textIO.newFloatInputReader()
                    .withDefaultValue(floatSuggestion(columnName))
                    .read(columnName);
        }

        private Double readDoubleColumn(String columnName) {
            return textIO.newDoubleInputReader()
                    .withDefaultValue(doubleSuggestion(columnName))
                    .read(columnName);
        }

        private String readStringColumn(boolean forceHex, String columnName) {
            return textIO.newStringInputReader()
                    .withDefaultValue(stringSuggestion(forceHex, columnName))
                    .read(columnName);
        }

        private Date readDateColumn(String columnName) {
            try {
                return dateFormat.parse(textIO.newStringInputReader()
                        .withDefaultValue(dateFormat.format(dateSuggestion(columnName)))
                        .read(columnName + " (" + dateFormatString + ")"));
            } catch (ParseException pe) {
                textIO.getTextTerminal().println("Invalid input format. Must use format:" + dateFormatString);
            }
            return readDateColumn(columnName);
        }
    }

    private static class RecordInputterWithRandomizedDefaults extends RecordInputter {

        private final Random r;

        RecordInputterWithRandomizedDefaults(TextIO textIO) {
            super(textIO);
            r = new Random();
        }

        @Override
        Integer intSuggestion(String columnName) {
            return r.nextInt();
        }

        @Override
        Long longSuggestion(String columnName) {
            return r.nextLong();
        }

        @Override
        Float floatSuggestion(String columnName) {
            return r.nextFloat();
        }

        @Override
        Double doubleSuggestion(String columnName) {
            return r.nextDouble();
        }

        @Override
        String stringSuggestion(boolean limitedToHex, String columnName) {
            // TODO: randomize the non-hex string
            return limitedToHex ? Long.toHexString(r.nextLong()) : "Hello, World!";
        }

        @Override
        Date dateSuggestion(String columnName) {
            return new Date(r.nextLong());
        }
    }

    private static class RecordInputterWithNoDefaults extends RecordInputter {

        RecordInputterWithNoDefaults(TextIO textIO) {
            super(textIO);
        }

        @Override
        Integer intSuggestion(String columnName) {
            return null;
        }

        @Override
        Long longSuggestion(String columnName) {
            return null;
        }

        @Override
        Float floatSuggestion(String columnName) {
            return null;
        }

        @Override
        Double doubleSuggestion(String columnName) {
            return null;
        }

        @Override
        String stringSuggestion(boolean limitedToHex, String columnName) {
            return null;
        }

        @Override
        Date dateSuggestion(String columnName) {
            return null;
        }
    }

    private static class RecordModel {

        private static AllTypesTable api = ForSure.allTypesTable().getApi();

        Long id;
        Boolean deleted;
        Date created;
        Date modified;
        Integer intColumn;
        Integer integerWrapperColumn;
        Long longColumn;
        Long longWrapperColumn;
        Float floatColumn;
        Float floatWrapperColumn;
        Double doubleColumn;
        Double doubleWrapperColumn;
        byte[] byteArrayColumn;
        String stringColumn;
        BigInteger bigIntegerColumn;
        BigDecimal bigDecimalColumn;
        Date dateColumn;

        static RecordModel fromRetriever(Retriever r) {
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

        @Override
        public String toString() {
            return "RecordModel{" +
                    "id=" + id +
                    ", deleted=" + deleted +
                    ", created=" + dateFormat.format(created) +
                    ", modified=" + dateFormat.format(modified) +
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
                    ", dateColumn=" + dateFormat.format(dateColumn) +
                    '}';
        }
    }
}
