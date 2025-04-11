package strategies;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.function.Function;

public class TimeBasedShardingStrategy<T> implements ShardingStrategy<T> {
    private final Function<T, LocalDate> dateExtractor;
    private final int shardsPerYear;

    public TimeBasedShardingStrategy(Function<T, LocalDate> dateExtractor, int shardsPerYear) {
        this.dateExtractor = dateExtractor;
        this.shardsPerYear = shardsPerYear;
    }

    @Override
    public int getShardId(T entity, Integer shardCount) {
        LocalDate date = dateExtractor.apply(entity);
        // TODO doesn't make sense for user, this must be configurable via builder, so that user can decide how he wants to shard his data, half-yearly/ quarterly/ maybe even monthly.
        int quarter = (date.get(IsoFields.QUARTER_OF_YEAR));
        return (quarter % shardsPerYear) % shardCount;
    }
}
