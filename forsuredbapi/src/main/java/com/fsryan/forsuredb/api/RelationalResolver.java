package com.fsryan.forsuredb.api;

public abstract class RelationalResolver<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> extends Resolver<U, R, G, S, F, O> {
    public RelationalResolver(ForSureInfoFactory<U, R> infoFactory) {
        super(infoFactory);
    }
}
