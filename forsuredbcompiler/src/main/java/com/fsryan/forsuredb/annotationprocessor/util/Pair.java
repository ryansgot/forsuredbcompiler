package com.fsryan.forsuredb.annotationprocessor.util;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Pair<F, S> {

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new AutoValue_Pair(first, second);
    }

    public abstract F first();
    public abstract S second();
}
