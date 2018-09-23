package com.fsryan.forsuredb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><i>IF YOU USE THIS IN COMBINATION WITH {@link FSIndex}, THEN
 * {@link Index} WILL BE IGNORED</i>
 *
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
 * @deprecated Due to the fact that you should be using {@link FSIndex}
 * instead. {@link FSIndex} is capable of using composites and
 * sort order to further specify your indices. This will be removed in 1.0.0.
 * @author Ryan Scott
 */
@Deprecated
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Index {
    /**
     * <p>Defaults to false. See {@link Index} for more information
     * @return whether this column should also be an index of the table
     */
    boolean unique() default false;
}
