package com.spring.c2.paymentService.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.c2.paymentService.entity.Payment;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;


@Configuration
@EnableCaching
public class RedisConfig {
	
	public RedisConfig() {
        System.out.println("RedisCacheConfig loaded 111");
    }
	
	@Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Primary
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     ObjectMapper redisObjectMapper) {

    	Jackson2JsonRedisSerializer<Payment> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Payment.class);
        valueSerializer.setObjectMapper(redisObjectMapper);

        // ✅ This ensures readable string keys like 'payment::1'
        RedisSerializationContext.SerializationPair<String> keySerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        RedisSerializationContext.SerializationPair<Payment> valuePair =
                RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer);

        RedisCacheConfiguration paymentCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySerializer)   // ✅ set key serializer
                .serializeValuesWith(valuePair)
                .entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("payment", paymentCacheConfig)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printCacheManagerInfo(ApplicationReadyEvent event) {
        CacheManager cacheManager = event.getApplicationContext().getBean(CacheManager.class);
        System.out.println(">>> CacheManager in use: " + cacheManager.getClass());
    }
    
    // Optional: log cache get errors (e.g. deserialization issues)
    @Bean
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                System.err.println("Cache GET error for key '" + key + "' in cache [" + cache.getName() + "]");
                exception.printStackTrace();
            }
        };
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void testDeserialization(ApplicationReadyEvent event) {
        RedisTemplate<String, Object> redisTemplate =
            (RedisTemplate<String, Object>) event.getApplicationContext().getBean("redisTemplate");

        Object raw = redisTemplate.opsForValue().get("payment::1");

        if (raw != null) {
            System.out.println(">>> Cached type: " + raw.getClass());
        } else {
            System.out.println(">>> Redis value is null");
        }
    }
}
