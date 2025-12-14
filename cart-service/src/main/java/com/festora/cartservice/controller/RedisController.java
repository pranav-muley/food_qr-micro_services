package com.festora.cartservice.controller;

import com.festora.cartservice.model.Cart;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/test/redis")
    public String test() {
        redisTemplate.opsForValue().set("test:key", new Cart());
        return "OK";
    }
}
