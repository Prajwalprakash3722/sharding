package me.devcoffee.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlShardUrlProvider implements ShardUrlProvider {

    private static final Pattern RANGE_PATTERN = Pattern.compile("\\{(\\d+)\\.\\.(\\d+)}");
    private final Map<String, String> expandedShardMap;

    public YamlShardUrlProvider(String filePath) {
        Yaml yaml = new Yaml(new Constructor(ShardConfigYaml.class, new LoaderOptions()));
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new RuntimeException("YAML file not found: " + filePath);
        }
        ShardConfigYaml configYaml = yaml.load(inputStream);
        this.expandedShardMap = expandShardMap(configYaml);
    }

    @Override
    public String apply(Integer shardId) {
        Optional<String> matchedKey = expandedShardMap.keySet().stream()
                .filter(k -> k.matches(".*?(\\d+)$") &&
                        Integer.parseInt(k.replaceAll(".*?(\\d+)$", "$1")) == shardId)
                .findFirst();

        return matchedKey.map(key -> expandedShardMap.get(key) + shardId)
                .orElseThrow(() -> new IllegalArgumentException("No mapping found for shard ID: " + shardId));
    }

    private Map<String, String> expandShardMap(ShardConfigYaml configYaml) {
        Map<String, String> expanded = new HashMap<>();
        for (Map.Entry<String, String> entry : configYaml.getShards().entrySet()) {
            Matcher matcher = RANGE_PATTERN.matcher(entry.getKey());
            if (matcher.find()) {
                int start = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));
                for (int i = start; i <= end; i++) {
                    String expandedKey = entry.getKey().replace(matcher.group(0), String.valueOf(i));
                    expanded.put(expandedKey, entry.getValue());
                }
            } else {
                expanded.put(entry.getKey(), entry.getValue());
            }
        }
        return expanded;
    }
}
