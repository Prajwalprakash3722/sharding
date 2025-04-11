package me.devcoffee;

import config.ShardingConfig;
import dao.User;
import strategies.HashingShardingStrategy;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) throws Exception {

        Function<User, String> userIdExtractor = User::getId;


        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>(userIdExtractor);

        // ideally these configs must come from a distributed storage, so that you don't have to stop the instance just to scale up / down / even blacklist the shards
        // something like redis / Memq would suffice
        Function<Integer, String> urlProvider = shardId -> "http://localhost:3306/user_shard_" + shardId + "?user=root&password=root";
        int shardCount = 64;

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(urlProvider).build();
        UUID uuid = UUID.randomUUID();
        User newUser = new User(uuid.toString(), "Prajwal P", LocalDate.ofYearDay(2025, 22));

        // hashing (most optimal way)
        // but what about when we want to clean up the data, can't leave data forever, will be expensive to clean up
        // what we can do is let this bundle route to a correct shard
        // in each shard we can create partitions on basis of date, etc...
        // very useful for data life cycle management
        // competitively faster as there are smaller tables

        int shardId = hashingConfig.determineShard(newUser);
        String shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);
        hashingConfig.updateShardCount(6);
        shardId = hashingConfig.determineShard(newUser);
        shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);
    }
}
