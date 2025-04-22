package me.devcoffee.strategies;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

public class HashingShardingStrategy<T> implements ShardingStrategy<T> {

    @Override
    public int getShardId(T entity, Integer shardCount) {
        String key = extractShardKey(entity);
        int hash = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asInt();
        // make hash positive
        hash = Math.abs(hash);
        // don't return 0
        return Math.max(1, hash % shardCount);
    }
}