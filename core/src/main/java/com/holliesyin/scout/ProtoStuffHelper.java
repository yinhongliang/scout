package com.holliesyin.scout;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by Hollies Yin on 2017-05-02.
 */
public class ProtoStuffHelper {
    public static byte[] serialize(Class clazz, Object obj) {
        Schema schema = RuntimeSchema.getSchema(clazz);
        LinkedBuffer buffer = LinkedBuffer.allocate(4096);
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(Class clazz, byte[] bytes) {
        Schema schema = RuntimeSchema.getSchema(clazz);
        try {
            T t = (T) clazz.newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, t, schema);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}