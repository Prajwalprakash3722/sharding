package me.devcoffee.strategies;

import me.devcoffee.annotations.ShardKey;

import java.lang.reflect.Field;

// All strategies must implement this interface
public interface ShardingStrategy<T> {
    int getShardId(T entity, Integer shardCount);

    default String extractShardKey(T entity) {
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
