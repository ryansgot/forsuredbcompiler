package com.fsryan.forsuredb.api;

public abstract class RelationalResolver<T extends RelationalResolver, U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<T, F>, O extends OrderBy<T, O>> extends Resolver<U, R, G, S, F, O> {
    public RelationalResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
