package com.fsryan.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.integrationtest.RandomString;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@FSTable("all_types")
@FSStaticData("all_types_static_data.xml")
public interface AllTypesTable extends FSGetApi {
    @FSColumn("int_column") @FSDefault("42") int intColumn(Retriever retriever);
    @FSColumn("integer_wrapper_column") @Index Integer integerWrapperColumn(Retriever retriever);
    @FSColumn("long_column") long longColumn(Retriever retriever);
    @FSColumn("long_wrapper_column") Long longWrapperColumn(Retriever retriever);
    @FSColumn("float_column") float floatColumn(Retriever retriever);
    @FSColumn("float_wrapper_column") Float floatWrapperColumn(Retriever retriever);
    @FSColumn("double_column") double doubleColumn(Retriever retriever);
    @FSColumn("double_wrapper_column") Double doubleWrapperColumn(Retriever retriever);
    @FSColumn("byte_array_column") byte[] byteArrayColumn(Retriever retriever);
    @FSColumn("string_column") @Unique(index = true) String stringColumn(Retriever retriever);
    @FSColumn("big_integer_column") BigInteger bigIntegerColumn(Retriever retriever);
    @FSColumn("big_decimal_column") BigDecimal bigDecimalColumn(Retriever retriever);
    @FSColumn("date_column") Date dateColumn(Retriever retriever);
    @FSColumn("boolean_column") boolean booleanColumn(Retriever retriever);
    @FSColumn("boolean_wrapper_column") Boolean booleanWrapperColumn(Retriever retriever);

    /**
     * <p>This is atypical for a table that is not a doc store. This is used to make data generation for tests easier.
     * If you want to serialize to objects, then I recommend using {@link com.fsryan.forsuredb.api.FSDocStoreGetApi}
     * as opposed to {@link FSGetApi}
     */
    @AutoValue
    abstract class Record {

        @AutoValue.Builder
        public static abstract class Builder {
            public abstract Builder bigDecimalColumn(@Nullable BigDecimal bigDecimalColumn);
            public abstract Builder bigIntegerColumn(@Nullable BigInteger bigIntegerColumn);
            public abstract Builder booleanColumn(boolean booleanColumn);
            public abstract Builder booleanWrapperColumn(@Nullable Boolean booleanWrapperColumn);
            public abstract Builder byteArrayColumn(@Nullable byte[] byteArrayColumn);
            public abstract Builder dateColumn(@Nullable Date dateColumn);
            public abstract Builder doubleColumn(double doubleColumn);
            public abstract Builder doubleWrapperColumn(@Nullable Double doubleWrapperColumn);
            public abstract Builder floatColumn(float floatColumn);
            public abstract Builder floatWrapperColumn(@Nullable Float floatWrapperColumn);
            public abstract Builder intColumn(int intColumn);
            public abstract Builder integerWrapperColumn(@Nullable Integer integerWrapperColumn);
            public abstract Builder longColumn(long longColumn);
            public abstract Builder longWrapperColumn(@Nullable Long longWrapperColumn);
            public abstract Builder stringColumn(@Nullable String stringColumn);
            public abstract Record build();
        }

        public static Record createRandom() {
            return createRandomBuilder().build();
        }

        public static Record createRandom(int byteLength, int stringLength) {
            return createRandomBuilder(byteLength, stringLength).build();
        }

        public static Builder createRandomBuilder() {
            return createRandomBuilder(
                    ThreadLocalRandom.current().nextInt(32) + 1,
                    // by default, strings are between 33 and 64 characters to minimize likelihood of collisions
                    ThreadLocalRandom.current().nextInt(32) + 33
            );
        }

        public static Builder createRandomBuilder(int byteLength, int stringLength) {
            final BigDecimal bigDecimal = new BigDecimal(Long.toString(ThreadLocalRandom.current().nextLong()) + '.' + Long.toString(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)));
            final BigInteger bigInteger = new BigInteger(Long.toString(ThreadLocalRandom.current().nextLong()));
            final boolean booleanColumn = ThreadLocalRandom.current().nextBoolean();
            final Boolean booleanWrapperColumn = ThreadLocalRandom.current().nextBoolean();
            final byte[] byteArrayColumn = new byte[Math.max(0, byteLength)];
            ThreadLocalRandom.current().nextBytes(byteArrayColumn);
            final Date dateColumn = new Date(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE));
            final double doubleColumn = ThreadLocalRandom.current().nextDouble() * ThreadLocalRandom.current().nextLong();
            final Double doubleWrapperColumn = ThreadLocalRandom.current().nextDouble() * ThreadLocalRandom.current().nextLong();
            final float floatColumn = ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextInt();
            final Float floatWrapperColumn = ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextInt();
            final int intColumn = ThreadLocalRandom.current().nextInt();
            final Integer integerWrapperColumn = ThreadLocalRandom.current().nextInt();
            final long longColumn = ThreadLocalRandom.current().nextLong();
            final Long longWrapperColumn = ThreadLocalRandom.current().nextLong();
            final String stringColumn = new RandomString(stringLength).nextString();
            return builder()
                    .bigDecimalColumn(bigDecimal)
                    .bigIntegerColumn(bigInteger)
                    .booleanColumn(booleanColumn)
                    .booleanWrapperColumn(booleanWrapperColumn)
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
                    .stringColumn(stringColumn);
        }

        public static Record create(@Nullable BigDecimal bigDecimal,
                                    @Nullable BigInteger bigInteger,
                                    boolean booleanColumn,
                                    @Nullable Boolean booleanWrapperColumn,
                                    @Nullable byte[] byteArrayColumn,
                                    @Nullable Date dateColumn,
                                    double doubleColumn,
                                    @Nullable Double doubleWrapperColumn,
                                    float floatColumn,
                                    @Nullable Float floatWrapperColumn,
                                    int intColumn,
                                    @Nullable Integer integerWrapperColumn,
                                    long longColumn,
                                    @Nullable Long longWrapperColumn,
                                    @Nullable String stringColumn) {
            return builder()
                    .bigDecimalColumn(bigDecimal)
                    .bigIntegerColumn(bigInteger)
                    .booleanColumn(booleanColumn)
                    .booleanWrapperColumn(booleanWrapperColumn)
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
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_AllTypesTable_Record.Builder();
        }

        @Nullable public abstract BigDecimal bigDecimalColumn();
        @Nullable public abstract BigInteger bigIntegerColumn();
        public abstract boolean booleanColumn();
        @Nullable public abstract Boolean booleanWrapperColumn();
        @Nullable public abstract byte[] byteArrayColumn();
        @Nullable public abstract Date dateColumn();
        public abstract double doubleColumn();
        @Nullable public abstract Double doubleWrapperColumn();
        public abstract float floatColumn();
        @Nullable public abstract Float floatWrapperColumn();
        public abstract int intColumn();
        @Nullable public abstract Integer integerWrapperColumn();
        public abstract long longColumn();
        @Nullable public abstract Long longWrapperColumn();
        @Nullable public abstract String stringColumn();
        @Nonnull public abstract Builder toBuilder();
    }
}