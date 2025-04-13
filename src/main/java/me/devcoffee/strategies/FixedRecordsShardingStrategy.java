package me.devcoffee.strategies;

import java.util.function.Function;

public class FixedRecordsShardingStrategy<T> implements ShardingStrategy<T> {
    private final Function<T, Integer> recordNumberExtractor;
    private final int recordsPerShard;

    public FixedRecordsShardingStrategy(Function<T, Integer> recordNumberExtractor, int recordsPerShard) {
        this.recordNumberExtractor = recordNumberExtractor;
        this.recordsPerShard = recordsPerShard;
    }

    @Override
    public int getShardId(T entity, Integer shardCount) {
        int recordNumber = recordNumberExtractor.apply(entity);
        return (int) Math.ceil((double) recordNumber / recordsPerShard) % shardCount;
    }
}

