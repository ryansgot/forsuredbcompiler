package com.fsryan.forsuredb.api;

public abstract class DocStoreResolver<B, T extends DocStoreResolver, U, R extends RecordContainer, G extends FSDocStoreGetApi<B>, S extends FSDocStoreSaveApi<U, B>, F extends Finder<T, F>, O extends OrderBy<T, O>> extends Resolver<U, R, G, S, F, O> {
    public DocStoreResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
