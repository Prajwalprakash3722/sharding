package strategies;

// All strategies must implement this interface
public interface ShardingStrategy<T> {
    int getShardId(T entity);
}
