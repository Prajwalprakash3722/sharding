package me.devcoffee.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ShardConfigYaml {
    private Map<String, String> shards;
}