package com.holliesyin.scout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Hollies Yin on 2017-04-28.
 */
@Service
public class GraphService {

    private final static Logger LOG = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    @Qualifier("cacheRedisTemplate")
    private RedisTemplate redisTemplate;

    public void load(List list, Class returnClazz, String graphName) {
        if (!GraphConfig.getGraphEnable()) {
            LOG.info("graph not enable,return");
            return;
        }
        LOG.info("enable graph.list size:{}", list == null ? 0 : list.size());
        Field[] fields = returnClazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(GraphKey.class)) {
                continue;
            }
            String graphRedisKey = GraphHelper.genRedisKey(graphName, field.getName());
            try {
                GraphHelper.saveInRedisHmap(list, returnClazz, field, graphRedisKey, redisTemplate);
            } catch (Exception e) {
                LOG.error("load data in redis fail,graphRedisKey:{}", graphRedisKey, e);
            }
        }
    }

    public <T> T query(String target, GraphRefresher refresher) {
        String graphName = refresher.getGraphName();
        String graphKey = refresher.getGraphKey();
        Class clazz = refresher.getClazz();

        String graphRedisKey = GraphHelper.genRedisKey(graphName, graphKey);

        if (!redisTemplate.hasKey(graphRedisKey)) {
            List list = refresher.load();
            load(list, clazz, graphName);
        }

        Object obj = redisTemplate.boundHashOps(graphRedisKey).get(target);
        return obj == null ? null : (T)ProtoStuffHelper.deserialize(clazz, (byte[]) obj);
    }
}