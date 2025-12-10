package com.gal.usc.roomify.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

// RedisHash para crear un espacio en Redis y timeToLive para ajustar el tiempo en segundos
@RedisHash(value = "refresh_tokens", timeToLive = 3600)
public class RefreshToken {

    @Id
    private String token; // UUID del refresh token
    private String username; // Usuario asociado

    @TimeToLive
    private long ttl;

    public RefreshToken() { }

    public RefreshToken(String token, String username, long ttl) {
        this.token = token;
        this.username = username;
        this.ttl = ttl;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
}