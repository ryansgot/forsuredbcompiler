package com.fsryan.forsuredb.api;

public abstract class DocStoreResolver<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends Resolver<U, R, G, S, F, O> {
    public DocStoreResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
