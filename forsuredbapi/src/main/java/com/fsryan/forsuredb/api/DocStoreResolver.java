package com.fsryan.forsuredb.api;

// 1. For OrderBy, and Finder, ensure that the Setter class is within type bounds
// 2. Update forsuredb's sqlitelib to correctly interpret @Index and @Unique annotations
// 3. Implement the initial serialization as default GSON serialization

public abstract class DocStoreResolver<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends Resolver<U, R, G, S, F, O> {
    public DocStoreResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
