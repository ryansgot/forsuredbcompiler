package com.fsryan.forsuredb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Marks a column as an index. This is not necessarily a unique index. If you want to make the index
 *     unique, then set {@link #unique()} to true. The default value for unique is false.
 * </p>
 * <p>
 *     Due to the fact that you can create a unique column that is not an index, there is also a
 *     {@link Unique} annotation. Annotating an {@link com.fsryan.forsuredb.api.FSGetApi} method with
 *     both {@link Unique} and {@link Index} will have the same effect as setting {@link #unique()} to
 *     true.
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Index {
    boolean unique() default false;
}
