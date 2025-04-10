package config;

import lombok.Getter;

import java.util.function.Function;

@Getter
public class ShardingConfig<T> {
    private final int shardCount;
    private final Function<Integer, String> shardUrlProvider;
    private final Function<T, String> keySelector;

    private ShardingConfig(int shardCount,
                           Function<Integer, String> shardUrlProvider,
                           Function<T, String> keySelector) {
        this.shardCount = shardCount;
        this.shardUrlProvider = shardUrlProvider;
        this.keySelector = keySelector;
    }

    public static class Builder<T> {
        private int shardCount;
        private Function<Integer, String> shardUrlProvider;
        private Function<T, String> keySelector;

        public Builder<T> withShardCount(int count) {
            this.shardCount = count;
            return this;
        }

        public Builder<T> withShardUrl(Function<Integer, String> provider) {
            this.shardUrlProvider = provider;
            return this;
        }

        public Builder<T> withKeySelector(Function<T, String> selector) {
            this.keySelector = selector;
            return this;
        }

        public ShardingConfig<T> build() {
            return new ShardingConfig<>(shardCount, shardUrlProvider, keySelector);
        }
    }
}
