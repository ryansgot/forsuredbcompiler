package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureJdbcInfoFactory;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.google.gson.Gson;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Date;
import java.util.function.BiConsumer;


public class ExampleApp {

    private static class AllTypesTableData implements BiConsumer<TextIO, String> {

        private static final String dateFormatString = "yyyy-MM-dd HH:mm:ss.SSS";
        private static final DateFormat dateFormat = new SimpleDateFormat(dateFormatString);

        @Override
        public void accept(TextIO textIO, String initialData) {
            TextTerminal<?> terminal = textIO.getTextTerminal();
            printGsonMessage(terminal, initialData);

            /*
    @FSColumn("date_column") Date dateColumn(Retriever retriever);
             */

            int intColumn = textIO.newIntInputReader()
                    .withDefaultValue(42)
                    .read("int_column");
            Integer integerWrapperColumn = textIO.newIntInputReader()
                    .withDefaultValue(42)
                    .read("integer_wrapper_column");
            long longColumn = textIO.newLongInputReader()
                    .withDefaultValue(42L)
                    .read("long_column");
            Long longWrapperColumn = textIO.newLongInputReader()
                    .withDefaultValue(42L)
                    .read("long_wrapper_column");
            float floatColumn = textIO.newFloatInputReader()
                    .withDefaultValue(4.2F)
                    .read("float_column");
            Float floatWrapperColumn = textIO.newFloatInputReader()
                    .withDefaultValue(4.2F)
                    .read("float_wrapper_column");
            double doubleColumn = textIO.newDoubleInputReader()
                    .withDefaultValue(42.42D)
                    .read("double_column");
            Double doubleWrapperColumn = textIO.newDoubleInputReader()
                    .withDefaultValue(42.42D)
                    .read("double_wrapper_column");
            byte[] byteArrayColumn = hexStringToByteArray(textIO.newStringInputReader()
                    .withDefaultValue("0123456789ABCDEF")
                    .read("byte_array_column (hex string)"));
            String stringColumn = textIO.newStringInputReader()
                    .withDefaultValue("Hello, World!")
                    .read("string_column");
            BigInteger bigIntegerColumn = null;
            try {
                bigIntegerColumn = new BigInteger(textIO.newStringInputReader()
                        .withDefaultValue("42")
                        .read("big_integer_column").replaceAll("[^0-9]", ""));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            BigDecimal bigDecimalColumn = null;
            try {
                bigDecimalColumn = new BigDecimal(textIO.newStringInputReader()
                        .withDefaultValue("42.42")
                        .read("big_decimal_column").replaceAll("[^0-9.]", ""));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            Date dateColumn = null;
            try {
                dateColumn = dateFormat.parse(textIO.newStringInputReader()
                        .withDefaultValue(dateFormat.format(new Date()))
                        .read("date_column (" + dateFormatString + ")"));
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            SaveResult<DirectLocator> result = ForSure.allTypesTable().set()
                    .bigDecimalColumn(bigDecimalColumn)
                    .bigIntegerColumn(bigIntegerColumn)
                    .byteArrayColumn(byteArrayColumn)
                    .dateColumn(dateColumn)
                    .doubleColumn(doubleColumn)
                    .doubleWrapperColumn(doubleWrapperColumn)
                    .floatColumn(floatColumn)
                    .floatWrapperColumn(floatWrapperColumn)
                    .intColumn(intColumn)
                    .integerWrapperColumn(integerWrapperColumn)
                    .longColumn(longColumn)
                    .longWrapperColumn(longWrapperColumn)
                    .stringColumn(stringColumn)
                    .save();

            String inserted = result.inserted() == null ? "" : "DirectLocator{table=" + result.inserted().table + ",id=" +result.inserted().id +"}";
            textIO.getTextTerminal().print("SaveResult{inserted=" + inserted + ",rowsAffected=" + result.rowsAffected() + ",exception=" +result.exception() + "}");

            textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
            textIO.dispose();
        }
    }


    public static void main(String[] args) {
        FSDBHelper.initDebug("jdbc:sqlite:example.db", null, TableGenerator.generate(), new FSDbInfoGsonSerializer());
        ForSure.init(ForSureJdbcInfoFactory.inst());

        TextIO textIO = TextIoFactory.getTextIO();
        new AllTypesTableData().accept(textIO, null);
    }

    static void printGsonMessage(TextTerminal<?> terminal, String initData) {
        if(initData != null && !initData.isEmpty()) {
            String message = new Gson().fromJson(initData, String.class);
            if(message != null && !message.isEmpty()) {
                terminal.println(message);
            }
        }
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
}
