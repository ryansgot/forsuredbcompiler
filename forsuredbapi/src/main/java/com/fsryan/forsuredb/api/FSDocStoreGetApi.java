package com.fsryan.forsuredb.api;

/**
 * <p>A special kind of {@link FSGetApi} that is intended for object
 * persistence as documents in either a {@link String} or byte array format.
 * An implementation of
 * {@link com.fsryan.forsuredb.api.adapter.FSSerializerFactory} is required
 * to be plugged in via the Java Service Provider Interface in order to
 * properly serialize/deserialize to/from either {@link String} or byte array.
 *
 * <p>Objects that you persist may use inheritance, but be sure to:
 * <ol>
 *   <li>
 *     Supply a {@link com.fsryan.forsuredb.api.adapter.FSSerializer} via the
 *     {@link com.fsryan.forsuredb.api.adapter.FSSerializerFactory} that can
 *     handle serializing/deserializing given inheritance.
 *   </li>
 *   <li>
 *     create a field on your extension of {@link FSDocStoreGetApi} that is a
 *     {@link Class} object of type T, the base type from which object stored
 *     in this doc store table will extend.
 *   </li>
 * </ol>
 *
 * <p>Why should you use this sort of table?
 * <ol>
 *   <li>
 *     You want to persist objects--possibly containing lots of data you would
 *     otherwise waste and add risk manually serializing to the database.
 *   </li>
 *   <li>
 *     You want a hybrid approach, where parts of your object, perhaps deeply
 *     nested, can be used for sorting, indexing, etc, but still achieve the
 *     advantages of a doc store API.
 *   </li>
 *   <li>
 *     You want to store multiple related types of data in the same table
 *   </li>
 * </ol>
 *
 * <p>The disadvantage of this approach is that it is brittle across class
 * name refactoring and name obfuscation. That is to say that if you release
 * a product storing objects of a class, then you should not change that class'
 * name or change the package in which it resides. Additionally, you should not
 * obfuscate the names of these classes because, with the exception of
 * {@link #getAsBaseType(Retriever)}, attempting to deserialize will fail
 * unless your deserializer is equipped to handle such renaming. The
 * {@link Class#forName(String)} method is used to pass along to your
 * deserializer on object retrieval and when you call
 * @link #getJavaClass(Retriever)}.
 *
 * @param <T> the base type from which all records will extend. If this type is
 * {@link Object}, then you will lose basic type-safety guarantees, but it's
 * not the end of the world, as the @link #className(Retriever)} method will be
 * able to track the java class name of the object.
 *
 * @author Ryan Scott
 */
public interface FSDocStoreGetApi<T> extends FSGetApi {

    /**
     * <p>Deserializes the document into a an object of type C
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @param <C> the type to deserialize
     * @return an object of type S deseralized from the {@link String} in the
     * doc column or the byte array in the blob_doc column
     * @see #doc(Retriever)
     */
    <C extends T> C getAs(Class<C> cls, Retriever retriever);

    /**
     * <p>Deserializes the document into a an object of the type stored. Even
     * though the compile-time type is the same as the parameterization, the
     * deserialization will occur on the type of the object when it was
     * originally stored.
     *
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @return an object of type T deseralized from the {@link String} in the
     * doc column or the byte array in the blob_doc column as the type it was
     * when it was stored.
     * @see #doc(Retriever)
     */
    T get(Retriever retriever);

    /**
     * <p>Deserializes the document into a an object of the base type T. You
     * may lose information if you chose this method of retrieval, but it is
     * guaranteed to work across class name refactors and obfuscation.
     *
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @return an object of type S deseralized from the string in the doc column
     * @see #doc(Retriever)
     */
    T getAsBaseType(Retriever retriever);

    /**
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @return the serialized string document (if your serializer serializes
     * to {@link String}), or null otherwise
     */
    String doc(Retriever retriever);

    /**
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @return the serialized string document (if your serializer serializes
     * to byte array), or null otherwise
     */
    byte[] blobDoc(Retriever retriever);

    // TODO: create a migration that is designed to update records whenever a class name changes
    /**
     * <p>Meta-data for the record that allows you to select records by the
     * java class of the object serialized to either {@link String} or byte
     * array.
     *
     * <p>This is used internally by forsuredb, passing it to your
     * implementation of {@link com.fsryan.forsuredb.api.adapter.FSSerializer}
     * in order to serialize/deserialize to/from the database.
     *
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @return the fully-qualified java class name of the serialized object
     * stored in this record
     * @see #getJavaClass(Retriever)
     */
    String className(Retriever retriever);

    /**
     * @param retriever a {@link Retriever} which points to a set of results
     *                  for this {@link FSDocStoreGetApi}
     * @param <C> an extension of the base class, T
     * @return a {@link Class} object that specifies the java class of the
     * object or null if the class could not be found. Null will typically be
     * returned only if the class name or package name has been changed since
     * the object was initially stored.
     * @see #className(Retriever)
     */
    <C extends T> Class<C> getJavaClass(Retriever retriever);
}
