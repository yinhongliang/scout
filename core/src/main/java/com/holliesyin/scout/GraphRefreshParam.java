package com.holliesyin.scout;

import java.util.Map;

/**
 * Created by Hollies Yin on 2017-05-05.
 */
public class GraphRefreshParam {
    private Map<String, Object> param;

    public GraphRefreshParam() {
    }

    public GraphRefreshParam(Map<String, Object> param) {
        this.param = param;
    }

    public void add(String key, Object value) {
        param.put(key, value);
    }

    public Object get(String key) {
        return param == null ? null : param.get(key);
    }
}