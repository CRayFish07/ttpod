package com.ttpod.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * CacheData is JavaMap .
 *
 * date: 2014/5/5 10:23
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class ZookeeperJavaMap<K,V>  extends ZookeeperCache<Map<K,V>> implements ICacheService<K,V>{

    protected Map<K,V> cache;

    protected void renderCacheData(Map<K,V> cache){
        this.cache = new ConcurrentHashMap<>(cache);
        log.info(" Refresh End , got {} rows.", cache.size());
    }

    public V get(K k){
        return cache.get(k);
    }

}
