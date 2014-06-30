package com.ttpod.cache.zoo;

import com.ttpod.cache.ICacheService;

import java.util.Collections;
import java.util.Map;

/**
 *
 * CacheData is JavaMap .
 *
 * date: 2014/5/5 10:23
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class ZookeeperJavaMap<K,V>  extends ZookeeperCache<Map<K,V>> implements ICacheService<K,V> {

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
