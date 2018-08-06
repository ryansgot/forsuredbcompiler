package com.fsryan.forsuredb.api;

public interface Conjunction<R extends Resolver> {

    interface And<R extends Resolver, T> extends Conjunction<R> {
        T and();
    }

    interface Or<R extends Resolver, T> extends Conjunction<R> {
        T or();
    }

    interface GroupableAndOr<R extends Resolver, T> extends And<R, T>, Or<R, T> {
        T startGroup();
        GroupableAndOr<R, T> endGroup();
    }

    R then();
}
