package me.devcoffee;

import me.devcoffee.config.ShardingConfig;
import me.devcoffee.strategies.HashingShardingStrategy;

import java.util.UUID;
import java.util.function.Function;

// following is a example of how to use this package
public class Main {
    public static void main(String[] args) throws Exception {

        record User(String id, String name) {}
        Function<User, String> userIdExtractor = user -> user.id();
        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>(userIdExtractor);

        Function<Integer, String> urlProvider = shardId -> "http://localhost:3306/user_shard_" + shardId + "?user=root&password=root";
        int shardCount = 64;

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(urlProvider).build();
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
