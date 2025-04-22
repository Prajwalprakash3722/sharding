package me.devcoffee.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlShardUrlProvider implements ShardUrlProvider {

    private static final Pattern SHARD_RANGE_PATTERN = Pattern.compile("\\{(\\d+)\\.\\.(\\d+)}");
    private final Map<Integer, String> originalShardIdToUrlMap;

    public YamlShardUrlProvider(String filePath) {
        this.originalShardIdToUrlMap = loadShardMappings(filePath);
    }

    @Override
    public String apply(Integer shardId) {
        String shardUrl = originalShardIdToUrlMap.get(shardId);
        if (shardUrl == null) {
            throw new IllegalArgumentException("No mapping found for shard ID: " + shardId);
        }
        return shardUrl;
    }


    private Map<Integer, String> loadShardMappings(String filePath) {
        Yaml yaml = new Yaml(new Constructor(ShardConfigYaml.class, new LoaderOptions()));
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new RuntimeException("YAML file not found: " + filePath);
        }

        ShardConfigYaml configYaml = yaml.load(inputStream);
        return expandShardMappings(configYaml.getShards());
    }

    private Map<Integer, String> expandShardMappings(Map<String, String> shardConfig) {
        Map<Integer, String> expandedMap = new HashMap<>();

        for (Map.Entry<String, String> entry : shardConfig.entrySet()) {
            String shardKey = entry.getKey();
            String shardUrl = entry.getValue();

            Matcher matcher = SHARD_RANGE_PATTERN.matcher(shardKey);
            if (matcher.find()) {
                int start = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));

                if (start > end) {
                    throw new IllegalArgumentException("Invalid shard range: " + shardKey);
                }

                for (int i = start; i <= end; i++) {
                    expandedMap.put(i, shardUrl);
                }
            } else {
                throw new IllegalArgumentException("Invalid shard key format: " + shardKey);
            }
        }

        return expandedMap;
    }
}