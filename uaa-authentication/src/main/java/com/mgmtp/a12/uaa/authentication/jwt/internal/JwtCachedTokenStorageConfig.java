package com.mgmtp.a12.uaa.authentication.jwt.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty("mgmtp.a12.uaa.authentication.cached-token-storage.enabled")
public class JwtCachedTokenStorageConfig {
}
