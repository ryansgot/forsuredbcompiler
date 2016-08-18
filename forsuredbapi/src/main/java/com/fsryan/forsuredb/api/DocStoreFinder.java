package com.fsryan.forsuredb.api;

public class DocStoreFinder<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends Finder<U, R, G, S, F, O> {
    public DocStoreFinder(DocStoreResolver<T, U, R, G, S, F, O> docStoreResolver) {
        super(docStoreResolver);
    }
}
