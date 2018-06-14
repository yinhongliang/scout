package com.holliesyin.scout;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hollies Yin on 2017-05-02.
 */
public class GraphHelper {
    private final static Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    private static final String REDIS_GRAPH_PREFIX = "GRAPH:CONSOLE";

    private static final String COLON = ":";

    public static String genRedisKey(String graphName, String graphKey) {
        return new StringBuilder(REDIS_GRAPH_PREFIX).append(COLON).append(graphName).append(COLON).append(graphKey).toString();
    }

    public static void saveInRedisHmap(List list, Class clazz, Field field, String graphRedisKey, RedisTemplate redisTemplate) throws IllegalAccessException {
        LOG.info("returnClazz:{},graphRedisKey:{}", clazz, graphRedisKey);
        if (redisTemplate.hasKey(graphRedisKey)) {
            LOG.info("already load data in redis,graphRedisKey:{}", graphRedisKey);
            return;
        }

        if (list == null || list.isEmpty()) {
            redisTemplate.boundHashOps(graphRedisKey).put(ObjectUtils.NULL, ObjectUtils.NULL);
            redisTemplate.expire(graphRedisKey, 1, TimeUnit.DAYS);
            return;
        }

        LOG.info("begin load data in redis,graphRedisKey:{}", graphRedisKey);
        Map<String, byte[]> hmap = new HashMap<>(GraphConfig.getGraphBatchSize());

        field.setAccessible(true);
        for (Object value : list) {
            hmap.put(field.get(value).toString(), ProtoStuffHelper.serialize(clazz, value));
            if (hmap.size() >= GraphConfig.getGraphBatchSize()) {
                redisTemplate.boundHashOps(graphRedisKey).putAll(hmap);
                hmap.clear();
                LOG.info("load in to graph success.graphRedisKey:{}", graphRedisKey);
            }
        }

        if (!hmap.isEmpty()) {
            redisTemplate.boundHashOps(graphRedisKey).putAll(hmap);
            hmap.clear();
        }

        redisTemplate.expire(graphRedisKey, 1, TimeUnit.DAYS);
    }
}