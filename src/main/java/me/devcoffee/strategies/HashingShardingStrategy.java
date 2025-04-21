package me.devcoffee.strategies;

import com.google.common.hash.Hashing;
import me.devcoffee.annotations.ShardKey;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class HashingShardingStrategy<T> implements ShardingStrategy<T> {

    @Override
    public int getShardId(T entity, Integer shardCount) {
        String key = extractShardKey(entity);
        int hash = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asInt();
        hash = Math.abs(hash);
        return Math.max(1, hash % shardCount);
    }

    private String extractShardKey(T entity) {
        String shardKeyValue = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ShardKey.class)) {
                // there should be only one shardKey in a DAO
                if (shardKeyValue != null) {
                    throw new IllegalArgumentException("Found multiple fields annotated with @ShardKey in class " + entity.getClass().getName());
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value != null) {
                        shardKeyValue = value.toString();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access shard key field", e);
                }
            }
        }
        if (shardKeyValue == null) {
            throw new IllegalArgumentException("No field annotated with @ShardKey found in class " + entity.getClass().getName());
        }
        return shardKeyValue;
    }
}