package com.fsryan.forsuredb.annotationprocessor.util;

import java.util.ArrayList;
import java.util.List;

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

    public <T> List<T> asListOf(Class<T> cls) {
        if (List.class.isAssignableFrom(uncasted.getClass())) {
            List<T> ret = new ArrayList<>();
            for (Object src: (List) uncasted) {
                addConstantToListAs(cls, ret, src);
            }
            return ret;
        }
        throw new IllegalStateException("Underlying type was " + uncasted.getClass() + "; cannot return it as a list of: " + cls);
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

    private static <T> void addConstantToListAs(Class<T> cls, List<T> dest, Object src) {
        String val = src.toString();
        val = val.substring(1, val.length() - 1);   // <-- strips beginning and ending "

        if (cls.equals(Integer.class)) {
            dest.add((T) Integer.valueOf(val));
        } else if (cls.equals(Long.class)) {
            dest.add((T) Long.valueOf(val));
        } else if(cls.equals(Float.class)) {
            dest.add((T) Float.valueOf(val));
        } else if (cls.equals(Double.class)) {
            dest.add((T) Double.valueOf(val));
        } else if (cls.equals(Short.class)) {
            dest.add((T) Short.valueOf(val));
        } else if (cls.equals(Boolean.class)) {
            dest.add((T) Boolean.valueOf(val));
        } else if (cls.equals(Byte.class)) {
            dest.add((T) Byte.valueOf(val));
        } else if (cls.equals(String.class)) {
            dest.add((T) String.valueOf(val));
        } else if (Enum.class.isAssignableFrom(cls)) {
            // TODO: not sure this works
            dest.add((T) Enum.valueOf(cls.asSubclass(Enum.class), src.toString()));
        } else {
            throw new IllegalArgumentException("cannot create list of " + cls + " from constant");
        }
    }
}
