package com.digitalwallet.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Fallback cache configuration used when no Redis-backed CacheManager has been defined.
 *
 * This ensures annotations that reference "redisCacheManager" still resolve even if
 * Redis is not configured (e.g. in environments without a Redis instance).
 */
@Configuration
@Profile("!test")
public class NoRedisCacheConfig {

  @Bean(name = "redisCacheManager")
  @ConditionalOnMissingBean(name = "redisCacheManager")
  public CacheManager redisCacheManagerFallback() {
    return new NoOpCacheManager();
  }
}
