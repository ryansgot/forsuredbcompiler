package com.fsryan.forsuredb.api;

public class DocStoreOrderBy<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends OrderBy<U, R, G, S, F, O> {
    public DocStoreOrderBy(DocStoreResolver<T, U, R, G, S, F, O> docStoreResolver) {
        super(docStoreResolver);
    }
}
