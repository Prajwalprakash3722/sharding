package me.devcoffee.config;

import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisShardUrlProvider implements ShardUrlProvider {

    private static final String SHARD_MAP_REDIS_KEY = "shard_map";
    private final Jedis jedis;

    public RedisShardUrlProvider(Jedis jedis, String fallbackYamlPath) {
        this.jedis = jedis;
        if (!"PONG".equalsIgnoreCase(jedis.ping())) {
            throw new RuntimeException("Redis is unreachable");
        }

        if (!jedis.exists(SHARD_MAP_REDIS_KEY)) {
            System.out.println("Redis empty. Loading from YAML.");
            YamlShardUrlProvider fallback = new YamlShardUrlProvider(fallbackYamlPath);
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                try {
                    String val = fallback.apply(i);
                    map.put("shard" + i, val);
                } catch (Exception ignored) {}
            }
            jedis.hset(SHARD_MAP_REDIS_KEY, map);
        }
    }

    @Override
    public String apply(Integer shardId) {
        Map<String, String> shardMap = jedis.hgetAll(SHARD_MAP_REDIS_KEY);
        Optional<String> matchedKey = shardMap.keySet().stream()
                .filter(k -> k.matches(".*?(\\d+)$") &&
                        Integer.parseInt(k.replaceAll(".*?(\\d+)$", "$1")) == shardId)
                .findFirst();

        return matchedKey.map(shardMap::get)
                .map(url -> url + shardId)
                .orElseThrow(() -> new IllegalArgumentException("No mapping found in Redis for shard ID: " + shardId));
    }
}
