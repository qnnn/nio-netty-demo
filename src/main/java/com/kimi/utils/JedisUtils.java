package com.kimi.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * @author 郭富城
 */
public class JedisUtils {

    public static JedisPool pool=null;

    static{
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        pool = new JedisPool(poolConfig,"127.0.0.1",6379);
    }

    /**
     * 对外提供静态方法
     */
    public static Jedis getJedis(){
        return pool.getResource();
    }
}
