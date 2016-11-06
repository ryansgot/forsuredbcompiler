package com.fsryan.forsuredb.api;

public interface Conjunction<R extends Resolver> {
    R then();

    interface And<R extends Resolver, T> extends Conjunction<R> {
        T and();
    }

    interface Or<R extends Resolver, T> extends Conjunction<R> {
        T or();
    }

    interface AndOr<R extends Resolver, T> extends And<R, T>, Or<R, T> {}
}
