package me.devcoffee;

import config.ShardingConfig;
import dao.User;
import strategies.FixedRecordsShardingStrategy;
import strategies.HashingShardingStrategy;
import strategies.TimeBasedShardingStrategy;

import java.time.LocalDate;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) throws Exception {

        Function<User, String> userIdExtractor = User::getId;
        Function<User, LocalDate> userDateFunction = User::getDate;
        Function<User, Integer> userIntegerFunction = User::getIdasInt;
        int shardCount = 10;

        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>(userIdExtractor, shardCount);
//        assuming that id means incremental ID, any iD that has UUID / anything won't work, should throw an exception
        FixedRecordsShardingStrategy<User> fixedRecordsShardingStrategy = new FixedRecordsShardingStrategy<>(userIntegerFunction, 10);
        TimeBasedShardingStrategy<User> timeBasedShardingStrategy = new TimeBasedShardingStrategy<>(userDateFunction, 4);

        Function<Integer, String> urlProvider = shardId -> "http://localhost:3306/user_shard_" + shardId + "?user=root&password=root";

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(urlProvider).build();
        ShardingConfig<User> fixedRecordBasedConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(fixedRecordsShardingStrategy).withShardUrlProvider(urlProvider).build();
        ShardingConfig<User> timeBasedConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(timeBasedShardingStrategy).withShardUrlProvider(urlProvider).build();

        User newUser = new User("12", "Prajwal P", LocalDate.ofYearDay(2025, 342));

//        hashing (most optimal way)
        int shardId = hashingConfig.determineShard(newUser);
        String shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);

//        fixedRecord (almost useless, because we need to generate the ID beforehand)
        shardId = fixedRecordBasedConfig.determineShard(newUser);
        shardUrl = fixedRecordBasedConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use Fixed Record Based Strategy with endpoint " + shardUrl);

//        timeBased (okays what if in april you got too much data?)
        shardId = timeBasedConfig.determineShard(newUser);
        shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use Time Based Strategy " + shardId + " with endpoint " + shardUrl);
    }
}
