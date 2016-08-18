package com.fsryan.forsuredb.api;

// 1. Create DocStoreResolverGenerator extension of ResolverGenerator                                   DONE
// 2. Create Appropriate DocStoreSetterGenerator Finder, and OrderBy classes
// 3. Amend ForSureGenerator to return DocStoreResolver types for Tables extending FSDocStoreGetApi
// 4. Amend FSAnnotationProcessor to appropriately call generator methods for @FSDocStore annotations
// 5. Update forsuredb's sqlitelib to correctly interpret @Index and @Unique annotations

public abstract class DocStoreResolver<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends Finder<U, R, G, S, F, O>, O extends OrderBy<U, R, G, S, F, O>> extends Resolver<U, R, G, S, F, O> {
    public DocStoreResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
