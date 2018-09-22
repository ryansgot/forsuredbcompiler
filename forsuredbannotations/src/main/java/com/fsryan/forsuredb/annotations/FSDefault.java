package com.fsryan.forsuredb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Set the default for a column.
 * <p>The compiler checks for sensibility of the default you set with respect
 * to Java values (not any specific DBMS). If you set a default for a column
 * that does not make sense, then the  compiler will throw an error. For
 * example, if your column is an integer, you set this value to "abc," a
 * compile error will be thrown caused by a {@link NumberFormatException}.
 *
 * <p>This annotation supports all types supported by forsuredb, but there is
 * no validation on {@link java.util.Date} because it is infeasible at the
 * compiler level to do the date format checking to validate input to
 * whichever DBMS is being used.
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface FSDefault {
    String value();
}
