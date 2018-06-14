package com.holliesyin.scout;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Hollies Yin on 2017-04-27.
 */
@Component
@Aspect
public class GraphAdvice {

    private final static Logger LOG = LoggerFactory.getLogger(GraphAdvice.class);
    @Autowired
    @Qualifier("cacheRedisTemplate")
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(GraphAspect)")
    public void graphPointcut() {
    }

    @AfterReturning(pointcut = "graphPointcut()", returning = "result")
    public void graph(JoinPoint joinPoint, Object result) {
        if (!GraphConfig.getGraphEnable()) {
            LOG.info("graph not enable,return");
            return;
        }
        LOG.info("enable graph.");

        Method method = MethodSignature.class.cast(joinPoint.getSignature()).getMethod();
        String targetName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        if (result == null) {
            LOG.info("result is null,return");
            return;
        }

        try {
            if (method.getReturnType() == List.class) {
                LOG.info("return type is list.");
                List list = (List) result;

                if (list.isEmpty()) {
                    LOG.info("return list is empty.");
                    return;
                }
                LOG.info("return list is not empty.");

                Type actualReturnType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                Class returnClazz = Class.forName(actualReturnType.getTypeName());

                LOG.info("returnClazz:{}", returnClazz);

                String graphKeyStr = "";
                for (Field field : returnClazz.getFields()) {
                    GraphKey graphKey = field.getDeclaredAnnotation(GraphKey.class);
                    if (graphKey != null) {
                        graphKeyStr = StringUtils.isBlank(graphKey.value()) ? field.getName() : graphKey.value();
                    }
                }

                if (StringUtils.isBlank(graphKeyStr)) {
                    graphKeyStr = "yyuid";
                }

                Field field = returnClazz.getField(graphKeyStr);

                String graphRedisKey = GraphHelper.genRedisKey(targetName,graphKeyStr);
                GraphHelper.saveInRedisHmap(list,returnClazz,field,graphRedisKey,redisTemplate);
            } else {
                LOG.info("return type is not list.");
            }
        } catch (Exception e) {
            LOG.error("load data in redis fail");
        }
    }
}