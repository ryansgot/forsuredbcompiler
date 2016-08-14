package com.fsryan.forsuredb.annotationprocessor.util;

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
     * @param cls The class of the type you would like to return
     * @param <T> The type you would like to return
     * @return The property cast to T
     * @throws ClassCastException if the type parameter cannot be used to cast the underlying object
     */
    public <T> T as(Class<T> cls) {
        return (T) uncasted;
    }

    /**
     * @param defaultValue the default value you would like to return if the object is null or a
     * @param <T> The type you would like to return
     * @return The property cast to T or defaultValue if a failure occurs
     */
    public <T> T castSafe(T defaultValue) {
        if (uncasted == null) {
            return defaultValue;
        }
        try {
            return (T) uncasted;
        } catch (ClassCastException cce) {
            APLog.w(Caster.class.getSimpleName(), "Cannot cast " + uncasted.getClass().getName() + " to " + (defaultValue == null ? "null" : defaultValue.getClass().getSimpleName()));
        }
        return defaultValue;
    }
}
