package me.devcoffee;

import config.ShardingConfig;
import dao.User;
import manager.ShardingManager;

import java.sql.Connection;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        ShardingConfig<User> config = new ShardingConfig.Builder<User>()
//                if you have 64 shards, god bless your SRE team :)
                .withShardCount(3)
//                ideally should be put in config file, but for poc seems good
                .withShardUrl(i -> "jdbc:mysql://localhost:3306/user_db_" + i + "?user=root&password=my-secret-pw")
                .withKeySelector(user -> String.valueOf(user.getId()))
                .build();
        ShardingManager<User> manager = new ShardingManager<>(config);
//       we manually passed the id here
        User u = new User("12321311233", "Prajwal P");
//
        String id = UUID.randomUUID().toString();
        User me = new User(id, "I am a UUID");
        System.out.println("User "+ me.getName()  +" with id "+ me.getId()  +" is on shard " + manager.getShardIdFor(me));
        System.out.println("User "+ u.getName()  +" is on shard " + manager.getShardIdFor(u));
    }
}
