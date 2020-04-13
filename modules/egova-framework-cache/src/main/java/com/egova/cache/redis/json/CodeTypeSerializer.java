package com.egova.cache.redis.json;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.flagwind.lang.CodeType;

import java.lang.reflect.Type;

/**
 * 自定义CodeType类型的序列化与反序列化实现
 */
public class CodeTypeSerializer implements ObjectSerializer, ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        T result = null;
        Object value = parser.parse();
        if (value == null) {
            return null;
        }
        try {
            String v;
            if (value instanceof JSONObject) {
                v = ((JSONObject) value).getString("value");
            } else {
                v = value.toString();
            }
            result = (T) ((Class) type).getConstructor(String.class).newInstance(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override
    public void write(JSONSerializer serializer, Object o, Object fieldName, Type fieldType, int features) {
        CodeType codeType = (CodeType) o;
        SerializeWriter out = serializer.getWriter();
        out.writeString(codeType.getValue());
    }
}