package com.forsuredb.annotationprocessor.util;

/**
 * <p>
 *     Gives the caller the choice of how it would like to get the property--either as an object
 *     ({@link #uncasted() uncasted()}) or casted to some other class.
 * </p>
 * @author Ryan Scott
 */
public class Caster {

    private Object uncasted;

    /*package*/ Caster(Object uncasted) {
        this.uncasted = uncasted;
    }

    public Object uncasted() {
        return uncasted;
    }

    public String asString() {
        return uncasted == null ? "null" : uncasted.toString();
    }

    /**
     * <p>
     *     Throws a {@link ClassCastException ClassCastException} if the type parameter cannot
     *     be used to cast the underlying object.
     * </p>
     * @param cls The class of the type you would like to return
     * @param <T> The type you would like to return
     * @return The property cast to T
     */
    public <T> T as(Class<T> cls) {
        return (T) uncasted;
    }
}
