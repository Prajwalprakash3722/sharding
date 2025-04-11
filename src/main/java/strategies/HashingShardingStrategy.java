package strategies;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class HashingShardingStrategy<T> implements ShardingStrategy<T> {
    private final Function<T, String> keyExtractor;
    private final int shardCount;

    public HashingShardingStrategy(Function<T, String> keyExtractor, int shardCount) {
        this.keyExtractor = keyExtractor;
        this.shardCount = shardCount;
    }

    @Override
    public int getShardId(T entity) {
        String key = keyExtractor.apply(entity);
        // Compute 128-bit hash and then convert it to a 32-bit integer for modulus
        int hash = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asInt();
        // Ensure a positive value before modulus
        hash = Math.abs(hash);
        // Make sure that hash is less than shardCount
        return hash % shardCount;
    }
}

