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