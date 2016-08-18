package com.fsryan.forsuredb.api;

/**
 * <p>
 *     You should never implement this class. There is a proxy implementation that will be invoked for
 *     all possible instances of this class.
 * </p>
 * @param <U> the record locator class (a Uri of some sort)
 * @param <T> The type of the object that is to be stored
 */
public interface FSDocStoreSaveApi<U, T> extends FSSaveApi<U> {
    /**
     * <p>
     *     Set the object to persist. The fully-qualified class name of the object as well as a
     *     serialized version of the object will be persisted when you call {@link #save()}
     * </p>
     * @param obj The object to persist
     * @return this same {@link FSDocStoreSaveApi}
     */
    FSDocStoreSaveApi<U, T> object(T obj);
}
