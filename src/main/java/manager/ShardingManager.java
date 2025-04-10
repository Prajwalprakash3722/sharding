package manager;

import com.google.common.hash.Hashing;
import config.ShardingConfig;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class ShardingManager<T> {
    private ShardingConfig<T> config;
    @Getter
    private final Map<Integer, String> shardUrls = new HashMap<>();

    public ShardingManager(ShardingConfig<T> config) {
        this.config = config;
        for (int i = 0; i < config.getShardCount(); i++) {
            shardUrls.put(i, config.getShardUrlProvider().apply(i));
        }
    }

    public int getShardIdFor(T item) {
        String key = config.getKeySelector().apply(item);
        long hash = Hashing.murmur3_128()
                .hashString(key, StandardCharsets.UTF_8)
                .asLong(); // Use lower 64-bits
        return (int) (Math.abs(hash) % config.getShardCount());
    }

    public Connection getConnectionFor(T item) throws Exception {
        int shardId = getShardIdFor(item);
        String url = shardUrls.get(shardId);
        return DriverManager.getConnection(url);
    }

    public void updateShardCount(int newCount) {
        config = new ShardingConfig.Builder<T>()
                .withShardCount(newCount)
                .withShardUrl(config.getShardUrlProvider())
                .withKeySelector(config.getKeySelector())
                .build();

        shardUrls.clear();
        for (int i = 0; i < newCount; i++) {
            shardUrls.put(i, config.getShardUrlProvider().apply(i));
        }
    }

    public int getCurrentShardCount() {
        return config.getShardCount();
    }
}
