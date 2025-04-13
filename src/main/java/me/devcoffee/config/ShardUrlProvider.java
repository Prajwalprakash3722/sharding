package me.devcoffee.config;

import java.util.function.Function;

public interface ShardUrlProvider extends Function<Integer, String> {
}
