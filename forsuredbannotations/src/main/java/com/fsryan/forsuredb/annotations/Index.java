package com.fsryan.forsuredb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Marks a column as an index. This is not necessarily a unique index. If
 * you want to make the index unique, then set {@link #unique()} to true or use
 * the {@link Unique} annotation in combination with this annotation.
 *
 * <p>Due to the fact that you can create an index that is not unique on some
 * DBMS implementations, there is also a {@link Unique} annotation. Thus, the
 * following are equivalent:
 * Using only {@link Index}:</br>
 * <pre><code>   public interface MyTable extends FSGetApi {
 *    {@literal @}FSColumn("my_column")
 *    {@literal @}Index(unique = true)
 *     String uuid(Retriever r)
 * }
 * </code></pre>
 * Using {@link Index} and {@link Unique}:</br>
 * <pre><code>   public interface MyTable extends FSGetApi {
 *    {@literal @}FSColumn("my_column")
 *    {@literal @}Index
 *    {@literal @}Unique
 *     String uuid(Retriever r)
 * }
 * </code> </pre>
 * Using only {@link Unique}:</br>
 * <pre><code>   public interface MyTable extends FSGetApi {
 *    {@literal @}FSColumn("my_column")
 *    {@literal @}Unique(index = true)
 *     String uuid(Retriever r)
 * }
 * </code></pre>
 *
 * <p>Composite indices are allowed, and they are defined in the same way
 * composite foreign keys are defined using {@link FSForeignKey#compositeId()}.
 * All methods marked with {@link Index}, having the same nonempty compositeId
 * will be part of the same composite index.
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Index {
    /**
     * <p>Defaults to false. See {@link Index} for more information
     * @return whether this column should also be an index of the table
     */
    boolean unique() default false;

    /**
     * <p>If this annotation is marking a column as part of a composite index,
     * Then you should provide a compositeId that is not the empty string.
     *
     * <p>Note that if you specify one member of the composite as unique, and
     * another member of the composite as not unique, the compiler will throw
     * an error.
     *
     * @return the compositeId string for this member of a composite index
     * @since 0.14.0
     */
    String compositeId() default "";

    /**
     * <p>This is DBMS-dependant, but many DBMS implementations allow you to
     * specify the sort order of the index. If not specified, the DBMS default
     * will be used.
     *
     * @return the sort order of the index
     * @since 0.14.0
     */
    String sortOrder() default "";
}
