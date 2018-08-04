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
 *           name refactoring. Additionally, you should not minify these classes, the class objects
 *           are reflectively instantiated with {@link Class#forName(String)}
 */
public interface FSDocStoreGetApi<T> extends FSGetApi {
    /**
     * <p>
     *     Deserializes the document into a an object of type C.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @param <C> the type to deserialize
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    <C extends T> C getAs(Class<C> cls, Retriever retriever);

    /**
     * <p>
     *     Deserializes the document into a an object of the type stored. Even though the compile-time
     *     type is the same as the parameterization, the deserialization will occur on the type of the
     *     object when it was originally stored.
     * </p>
     * <p>
     *     Note that this will most-likely lead to errors if you refactor the class name after storage.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    T get(Retriever retriever);

    /**
     * <p>
     *     Deserializes the document into a an object of the base type T. You may lose information if you
     *     chose this method of retrieval, but it is guaranteed to work across class name refactors.
     * </p>
     * <p>
     *     Note that this will most-likely lead to errors if you change the fields of this class and their
     *     serialization
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    T getAsBaseType(Retriever retriever);

    /**
     * <p>
     *     Use this only if you want to perform your own deserialization and that the initial serialization
     *     was to a String.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return A string representation of the document
     */
    @FSColumn("doc") String doc(Retriever retriever);

    /**
     * <p>
     *     Use this only if you want to perform your own deserialization and that the original serialization
     *     was into a byte array.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @return A byte array representation of the object
     */
    @FSColumn("blob_doc") byte[] blobDoc(Retriever retriever);

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
     * @see #getJavaClass(Retriever)
     */
    @FSColumn("class_name") String className(Retriever retriever);

    /**
     * <p>
     *     Use this method to get the Class object of the java class that is stored in this record.
     * </p>
     * @param retriever a {@link Retriever} which points to a set of results for this {@link FSDocStoreGetApi}
     * @param <C> an extension of the base class, T
     * @return a {@link Class} object that specifies the java class of the object or null if the class could
     * not be found. Null will typically be returned only if the class name or package name has been changed
     * since the object was initially stored.
     * @see #className(Retriever)
     */
    <C extends T> Class<C> getJavaClass(Retriever retriever);
}
