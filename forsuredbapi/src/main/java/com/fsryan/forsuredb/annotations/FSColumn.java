/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.annotations;

import com.fsryan.forsuredb.api.FSGetApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Use the FSColumn annotation on methods defined in your extensions of
 *     {@link FSGetApi FSGetApi} in order to specify the column name associated
 *     with the method.
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface FSColumn {
    /**
     * @return the name of the column
     */
    String value();

    /**
     * <p>Defaults to true. changing to false will cause the compiler to fail to generate the appropriate
     * method that will allow you to filter results by this column. In other words, if set to false,
     * queries will not contain WHERE clauses with this column.
     *
     * <p>Why would you want to flip this to false? forsuredbcompiler will generate methods as part of the
     * fluent api for querying allowing you filter results by some property of the column. For some
     * data types, there are several methods that get generated. You may not want to generate methods
     * that you will never use.
     *
     * <p>Note that flipping this to false will mean that you cannot filter your results by this column
     * at all.
     *
     * @return whether the table is searchable by this column
     */
    boolean searchable() default true;

    /**
     * <p>Defaults to true. changing to false will cause the compiler to fail to generate the appropriate
     * method that will allow you to order results by this column. In other words, if set to false,
     * queries will not contain ORDER BY clauses including this column
     *
     * <p>Why would you want to flip this to false? forsuredbcompiler will generate methods as part of the
     * fluent api for querying allowing you to set arbitrary orders to your data set. However, you may
     * not want to generate methods that you will not use.
     *
     * @return whether retrieval of records may be ordered by this column
     */
    boolean orderable() default true;

    /**
     * <p>This is a powerful construct of the forsuredb framework that facilitates a hybrid of
     * doc store and relational database features. It allows you to store columns beside your
     * serialized object that contain values pulled out of that object. The value for that column
     * can be nested arbitrarily deep within the object that is getting serialized. This property
     * is the way in which you tell forsuredb how to access the value of this column on the object
     * that is being stored in the doc store table.
     *
     * <p>Why should you use this?
     * <ol>
     *     <li>
     *         You want to access data in an object you've stored in a doc store table without
     *         deserializing the document first.
     *     </li>
     *     <li>
     *         You want to sort records from this table based upon data that in the object that
     *         was stored in this table without having to deserialize all of the documents of all
     *         records that come back on a query (you can use {@link Index} to make the sort
     *         super-fast).
     *     </li>
     *     <li>
     *         You want to look up documents by some index (you can use {@link Index} to make the
     *         lookup super-fast).
     *     </li>
     * </ol>
     *
     * <p>These strings are the names of methods that must be called in order to pull out the value of the
     * property from an object (that is to be serialized) into the data store. These methods
     * <i>MAY NOT</i> take parameters. For example, if you had a value object like this:
     * <pre>{@code public class MyClass {
     *           public static class MyInnerClass {
     *               private String myInnerString;
     *               private int myInnerInt;
     *
     *               public String getMyInnerString() {
     *                   return myInnerString;
     *               }
     *
     *               public int getMyInnerInt() {
     *                   return myInnerInt;
     *               }
     *           }
     *
     *           private String strValue;
     *           private MyInnerClass myInnerClass;
     *
     *           public String getStrValue() {
     *               return strValue;
     *           }
     *
     *           public MyInnerClass getMyInnerClass() {
     *               return myInnerClass;
     *           }
     *       }
     *     }</pre>
     *
     * And you wanted to include the {@code myInnerInt} field of the {@code MyClass.MyInnerClass} class and
     * the {@code strValue} object as columns in a document store table, then you would define your columns
     * like this:
     * <pre><code>public interface MyDocStoreTable extends FSDocStoreGetApi<MyClass> {
     *     Class BASE_CLASS = MyClass.class;
     *
     *     {@literal @}FSColumn(
     *         value = "inner_int_column",  // <-- the column name
     *         documentValueAccess = {"getMyInnerClass", "getMyInnerInt"}   // <-- how to access the value
     *     )
     *     int myInnerInt(Retriever retriever);
     *     {@literal @}FSColumn(
     *         value = "str_value_column",  // <-- the column name
     *         documentValueAccess = "getStrValue"  // <-- how to access the value
     *     )
     *     String strValue(Retriever retriever);
     * }
     * </code></pre>
     *
     * <p>The above would tell the generated
     * {@link com.fsryan.forsuredb.api.FSDocStoreSaveApi FSDocStoreSaveApi} implementation that it needs to
     * set the inner_int_column value by calling {@code obj.getMyInnerClass().getMyInnerInt()} and the
     * str_value_column value by calling {@code obj.getStrValue()}.
     *
     * <p>Here are a few things to keep in mind:
     * <ul>
     *     <li>
     *         If you don't specify this property, then the framework won't insert the value drawn from the
     *         object that you insert/update. It is useful to not specify this property when the column you
     *         add is just a foreign key to some other table or the column data does not come any value
     *         composed in the object you are storing.
     *     </li>
     *     <li>
     *         You can specify arbitrarily deep method call chains, but <i>EACH METHOD MUST BE A NO-ARG
     *         METHOD</i>
     *     </li>
     *     <li>
     *         This property only has meaning if your interface is an extension of
     *         {@link com.fsryan.forsuredb.api.FSDocStoreGetApi FSDocStoreGetApi}.
     *     </li>
     * </ul>
     *
     *<p>Note that the following types are supported for column values:
     * <ul>
     *     <li>int and {@link Integer}</li>
     *     <li>long and {@link Long}</li>
     *     <li>float and {@link Float}</li>
     *     <li>double and {@link Double}</li>
     *     <li>byte[]</li>
     *     <li>{@link String}</li>
     *     <li>{@link java.util.Date Date}</li>
     *     <li>{@link java.math.BigInteger BigInteger}</li>
     *     <li>{@link java.math.BigDecimal} BigDecimal</li>
     * </ul>
     * At this time, you cannot use any other type as a column value.
     *
     * @return the names of each method to call on the object to be stored as a document in order to
     * store values of that object in columns of the record containing the document.
     */
    String[] documentValueAccess() default "";
}
