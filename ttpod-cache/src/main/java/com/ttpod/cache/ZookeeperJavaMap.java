package com.ttpod.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 定时任务把计算结果存到zookeeper中
 *
 * 客户机自动从zookeeper获取更新.
 *
 * ZooKeeper Node limit 1 Mb.
 *
 * If node Data > 1Mb. pls save in Mongo.
 * @see com.ttpod.cache.ZookeeperJavaMapInMongo
 *
 *
 * date: 2014/5/5 10:23
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class ZookeeperJavaMap<K,V> implements ICacheService<K,V>{


    static final boolean isTest = Boolean.getBoolean("env.test");

    @Resource
    protected CuratorFramework curatorFramework;

    protected   Logger log = LoggerFactory.getLogger(getClass());

    NodeCache nodeCache;

    protected String dataKey(){
        return "/JavaMap/"+getClass().getName();
    }

    protected Map<K,Object> cache;

    protected void renderCacheData(Map<K,Object> cache){
        this.cache = cache;
    }

    public V get(K k){
        return (V)cache.get(k);
    }


    public int refresh() {

        log.info(" Begin  Refresh ..");
        if (isTest) {
            return 0;
        }
        byte[] mapData = loadByteObjects(nodeCache.getCurrentData());
        Map<K, Object> value =  byte2map(mapData);
        if(null != value){
            renderCacheData(new ConcurrentHashMap<>(value));
            log.info(" Refresh End , got {} rows.", value.size());
            return value.size();
        }else{
            log.info(" Refresh End(Error) , got NULL.");
            return 0;
        }
    }



    @PostConstruct
    protected void init(){
        nodeCache = new NodeCache(curatorFramework, dataKey());
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                refresh();
            }
        });
        try {
            nodeCache.start(true);
        } catch (Exception e) {
            log.error("Start NodeCache error for path: {}, error info: {}",  dataKey(), e.getMessage());
        }
        refresh();
    }

    protected byte[] loadByteObjects(ChildData node){
        return node.getData();
    }


     Map<K,Object> byte2map(byte[] data){
        if(null != data && data.length > 0){
            try (ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(data))){
                return (Map)inp.readObject();
            } catch (Exception e) {
                log.error("deser byte[] to Map Error",e);
            }
        }
        return null;
    }

}
