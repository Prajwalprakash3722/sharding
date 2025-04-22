package me.devcoffee;

import me.devcoffee.annotations.ShardKey;
import me.devcoffee.config.ShardUrlProvider;
import me.devcoffee.config.YamlShardUrlProvider;
import me.devcoffee.config.ShardingConfig;
import me.devcoffee.strategies.HashingShardingStrategy;

import java.util.UUID;

// following is an example of how to use this package
public class Main {
    public static void main(String[] args) throws Exception {

        record User(@ShardKey String id, String name) {}
        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>();
        ShardUrlProvider shardUrlProvider = new YamlShardUrlProvider("config.yaml");
        int shardCount = 16;

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(shardUrlProvider).build();
        UUID uuid = UUID.randomUUID();
        User newUser = new User(uuid.toString(), "Prajwal P");

        int shardId = hashingConfig.determineShard(newUser);
        String shardUrl = hashingConfig.getShardUrl(shardId);

        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);
        // if you change the shard count, all data must be rehashed to map to the new shards, very expensive, so plan your capacity very carefully
        // TODO, implement a way to rehash the data
        // ? what should we do if a shard goes down?, we can blacklist it but ofc it will affect the count, need to think a bit here
        hashingConfig.updateShardCount(6);
        shardId = hashingConfig.determineShard(newUser);
        shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy after updating the shardCount with endpoint " + shardUrl);
    }
}
