# Sharding

> This is me trying to figure out how to scale big databases without losing my mind.

## What is Sharding?

Sharding is the process of **splitting ordered data horizontally across multiple MySQL nodes**. It helps in distributing the load and scaling databases effectively.

---

## How Does Sharding Work?

There’s no single "best" way to shard—depends on your use case. But here are a few common patterns:

### 1. Sharding Based on Time (e.g., Months or Quarters)

- You can decide to move to a new shard every quarter (or any interval your team prefers).
- This approach is straightforward and works well for operations like truncates, archiving, and partition management.

### 2. Sharding Based on Fixed Number of Records

- You can define a limit for how many records a single shard can hold.  
  For example, insert up to **1000 users in one shard**.
- The **1001st user** goes into **shard 2**, and so on.
- While simple to implement, this method is **not highly scalable**, and although it allows some degree of both vertical and horizontal scaling, it may not be optimal for large-scale systems.

### 3. Sharding Based on Hashing (Recommended)

- This is one of the **most reliable** and scalable methods.
- Choose a column (like user ID) and apply a hash function on it to determine which shard to insert into.
- A good example is using `Hashing.murmur3_128()` from the Guava library.

**Example:**

```text
- Available shards: user_shard1, user_shard2, user_shard3, user_shard4
- User ID: 0af9758d-8bcc-4248-b182-e7cc8aa948de
- Apply the hash function on the user ID
- Result: User with ID 0af9758d-8bcc-4248-b182-e7cc8aa948de is assigned to shard 1 → user_shard1
```

#### But Wait—What About Cleanup?

Can’t just let data pile up forever. Cleaning millions of records = slow and expensive.

##### Solution:
Use sharding plus partitioning.
Each shard can have internal partitions based on date or whatever makes sense for your data.
- This lets you:
  - Drop old partitions in seconds
  - Keep table sizes small
  - Speed up queries


### Example

```java
public class Main {
    public static void main(String[] args) throws Exception {
        // this is your data schema (POJO)
        record User(String id, String name) {}
        // this is your key extractor to submit to the hashing method
        Function<User, String> userIdExtractor = user -> user.id();
        // this is your strategy
        HashingShardingStrategy<User> hashingStrategy = new HashingShardingStrategy<>(userIdExtractor);
        // yaml shardProvider (read down below to know more about this)
        ShardUrlProvider shardUrlProvider = new YamlShardUrlProvider("config.yaml");
        // redis shardProvider (read down below to know more about this)
        ShardUrlProvider redisUrlProvider = new RedisShardUrlProvider(new Jedis("localhost", 6379), "config.yaml");
        // total no of shards
        int shardCount = 16;

        ShardingConfig<User> hashingConfig = new ShardingConfig.Builder<User>().withShardCount(shardCount).withShardingStrategy(hashingStrategy).withShardUrlProvider(redisUrlProvider).build();
        
        UUID uuid = UUID.randomUUID();
        User newUser = new User(uuid.toString(), "Prajwal P");


        int shardId = hashingConfig.determineShard(newUser);
        String shardUrl = hashingConfig.getShardUrl(shardId);
        System.out.println("User will be inserted into shard " + shardId + " if we use HashBased Strategy with endpoint " + shardUrl);
        // SQL Execute
    }
}
```

### Why ShardUrlProvider?

In distributed systems, it's common to split data across multiple database shards. Each shard might reside on a different host, database instance, or even be served by a different engine altogether.

The ShardUrlProvider abstraction is designed to give users flexibility in how shard-to-URL mappings are defined and retrieved. It enables:

- Decoupling: Logic that uses shards doesn't need to know where or how the mapping is stored.
- Dynamic updates: Switch or update shard mappings without changing your core business logic.
- Pluggability: Use YAML, Redis, a database, or implement your own custom strategy — all through a common interface.

#### This is especially useful when:

- Some shards live on one DB, others on another.
- You want to dynamically add/remove/update shards without a restart.
- You want to support both static (file-based) and dynamic (remote store) configs with ease.

#### By default, we provide:

- `YamlShardUrlProvider`: Simple file-based configuration
- `RedisShardUrlProvider`: For environments where centralized and dynamic config is preferred
Need something else? Just implement the interface and plug in your own logic.

#### How to decide what you want?

1. High Availability Required?
   - If your application cannot afford downtime, we recommend storing shard configurations in a distributed database like Redis.

  - Why use a database for something so simple?
    - Dynamic updates: Easily add or remove shards without restarting the application.
    - Graceful degradation: If a shard goes down, simply mark it as inactive in the DB to prevent routing traffic to it.
    - Flexibility: Update shard URLs on the fly (even if rare).
  
  Trade-offs:
    - Introduces an extra layer of complexity (e.g., managing Redis itself, which also needs to be highly available).

2. Downtime is Acceptable?
   - If some downtime is acceptable, a static YAML file is sufficient.
   - Simple to maintain.
   - Any changes (add/remove/update shards) just require editing the YAML file and reloading the server.