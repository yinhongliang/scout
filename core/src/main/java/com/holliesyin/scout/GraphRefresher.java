package com.holliesyin.scout;

import java.util.List;

/**
 * Created by Hollies Yin on 2017-05-08.
 */
public abstract class GraphRefresher<T> {

    public GraphRefresher() {
    }

    public abstract Class getClazz();

    public abstract Class getSubClass();

    public abstract List load();

    public String getGraphName() {
        return getSubClass().getSimpleName();
    }

    public String getGraphKey() {
        GraphKey graphKeyObj = (GraphKey)getClazz().getAnnotation(GraphKey.class);
        return graphKeyObj.value();
    }
}