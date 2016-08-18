package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.annotations.FSColumn;

/**
 * <p>
 *     A special kind of {@link FSGetApi} that is intended for storage of documents in a String (Text)
 *     format.
 * </p>
 * <p>
 *     Why would you want to use this sort of table?
 *     <ul>
 *         <li>
 *             You wish to persist objects with resistance to addition/subtraction of fields and
 *             openness to change and extension across versions of your software.
 *         </li>
 *         <li>
 *             You wish to persist/replay streams of related events. In this case, the type parameter
 *             would be some generic base class from which potentially many extensions could be used
 *             to describe different types of events.
 *         </li>
 *     </ul>
 * </p>
 * @param <T> the base type from which all records will extend. If this type is {@link Object}, then
 *           you will lose basic type-safety guarantees, but it's not the end of the world, as the
 *           somewhat flimsy {@link #className(Retriever)} method will be able to track the java
 *           class of the object. The downfall of this approach is that it is brittle across class
 *           name refactoring.
 */
public interface FSDocStoreGetApi<T> extends FSGetApi {
    /**
     * <p>
     *     Deserializes the document into a an object of type C.
     * </p>
     * <p>
     *     You must make an assignment with this call for S to be non-ambiguously inferred. In other
     *     words, you must assign a temporary variable and then call a method on that in order for
     *     the compiler to know about additional members of the object that are not guaranteed by
     *     the base type, T.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @param <C> the type to deserialize
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    <C extends T> C getAs(Class<C> cls, Retriever retriever);

    /**
     * <p>
     *     Deserializes the document into a an object of type T.
     * </p>
     * <p>
     *     You must make an assignment with this call for S to be non-ambiguously inferred. In other
     *     words, you must assign a temporary variable and then call a method on that in order for
     *     the compiler to know about additional members of the object that are not guaranteed by
     *     the base type, T.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @param <C> the type to deserialize
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    T get(Retriever retriever);

    /**
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return A string representation of the document
     */
    @FSColumn("doc") String doc(Retriever retriever);

    // TODO: create a migration that is designed to update records whenever a class name changes
    /**
     * <p>
     *     Meta-data for the record that allows you to select all records by their java class. This
     *     column has nothing to do with serialization/deserialization. And thus, if you refactor
     *     the package name or the class name for a class, then this will not be reflected in this
     *     column . . . yet.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return the fully-qualified java class name of the
     */
    @FSColumn("class_name") String className(Retriever retriever);
}
