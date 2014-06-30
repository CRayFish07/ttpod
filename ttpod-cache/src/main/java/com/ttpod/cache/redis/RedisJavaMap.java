package com.ttpod.cache.redis;

import com.ttpod.cache.ICacheService;

import java.util.Collections;
import java.util.Map;

/**
 * date: 14-6-30 16:33
 *
 * @author: yangyang.cong@ttpod.com
 */
public class RedisJavaMap <K,V>  extends RedisCache<Map<K,V>> implements ICacheService<K,V> {

    protected volatile Map<K,V> cache;

    @Override
    public synchronized void renderCacheData(Map<K,V> cache){

        if(null == cache){
            this.cache = Collections.emptyMap();
        }else{
//            this.cache = new ConcurrentHashMap<>(cache);
            Map old = this.cache;
            this.cache = cache;
            log.info(" Refresh End , got {} rows.", cache.size());
            if(null != old) old.clear();
        }
    }

//    protected <Value>void renderCacheData(Map<K,Value> cache){
//        this.cache = new ConcurrentHashMap<>(cache.size());
//        this.cache.putAll(cache);
//        log.info(" Refresh End , got {} rows.", cache.size());
//    }

    public V get(K k){
        return cache.get(k);
    }

}

