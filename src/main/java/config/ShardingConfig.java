package config;

import strategies.ShardingStrategy;

import java.util.function.Function;

public class ShardingConfig<T> {
    private final ShardingStrategy<T> strategy;
    private final Function<Integer, String> shardUrlProvider;

    private ShardingConfig(int shardCount,
                           ShardingStrategy<T> strategy,
                           Function<Integer, String> shardUrlProvider) {
        this.strategy = strategy;
        this.shardUrlProvider = shardUrlProvider;
    }

    public int determineShard(T entity) {
        return strategy.getShardId(entity);
    }

    public String getShardUrl(int shardId) {
        return shardUrlProvider.apply(shardId);
    }

    public static class Builder<T> {
        private int shardCount;
        private ShardingStrategy<T> strategy;
        private Function<Integer, String> shardUrlProvider;

        public Builder<T> withShardCount(int count) {
            this.shardCount = count;
            return this;
        }

        public Builder<T> withShardingStrategy(ShardingStrategy<T> strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder<T> withShardUrlProvider(Function<Integer, String> provider) {
            this.shardUrlProvider = provider;
            return this;
        }

        public ShardingConfig<T> build() throws Exception {
            if (strategy == null) {
                throw new Exception("Sharding strategy must be provided");
            }
            if (shardUrlProvider == null) {
                throw new Exception("Shard URL provider must be provided");
            }
            return new ShardingConfig<>(shardCount, strategy, shardUrlProvider);
        }
    }
}
