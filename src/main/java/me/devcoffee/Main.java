package me.devcoffee;

import me.devcoffee.config.RedisShardUrlProvider;
import me.devcoffee.config.ShardUrlProvider;
import me.devcoffee.config.YamlShardUrlProvider;
import me.devcoffee.config.ShardingConfig;
import me.devcoffee.strategies.HashingShardingStrategy;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.function.Function;

// following is an example of how to use this package
public class Main {
    public static void main(String[] args) throws Exception {

        record User(String id, String name) {}
        Function<User, String> userIdExtractor = user -> user.id();
        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>(userIdExtractor);
        ShardUrlProvider shardUrlProvider = new YamlShardUrlProvider("config.yaml");
        ShardUrlProvider redisUrlProvider = new RedisShardUrlProvider(new Jedis("stg-ppsde002.phonepe.mh6", 6379), "config.yaml");
        int shardCount = 16;

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(redisUrlProvider).build();
        UUID uuid = UUID.randomUUID();
        User newUser = new User(uuid.toString(), "Prajwal P");


        int shardId = hashingConfig.determineShard(newUser);
        String shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);
        // if you change the shard count, all data must be rehashed to map to the new shards, very expensive, so plan your capacity very carefully
        hashingConfig.updateShardCount(6);
        shardId = hashingConfig.determineShard(newUser);
        shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy after updating the shardCount with endpoint " + shardUrl);
    }
}
