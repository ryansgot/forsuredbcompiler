package com.fsryan.forsuredb.annotationprocessor.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

@AutoValue
public abstract class Pair<F, S> {

    public static <F, S> Pair<F, S> create(@Nonnull F first, @Nonnull S second) {
        return new AutoValue_Pair<>(first, second);
    }

    @Nonnull public abstract F first();
    @Nonnull public abstract S second();
}
