package com.fsryan.forsuredb.api;

public abstract class DocStoreResolver<T extends DocStoreResolver, B, U, R extends RecordContainer, G extends FSDocStoreGetApi<B>, S extends FSDocStoreSaveApi<U, B>, F extends Finder<T, F>, O extends OrderBy<T, O>> extends Resolver<T, U, R, G, S, F, O> {
    public DocStoreResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
