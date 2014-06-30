package com.ttpod.cache.zoo;

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
public abstract class CuratorZookeeperCache<Obj>{


    static final boolean isTest = Boolean.getBoolean("env.test");

    static final String NAME_SPACE = System.getProperty("cache.ns","").trim();

    static final String PREFIX = "/JavaMap/";


    static String nameSpace = PREFIX;

    static {
        setNameSpace(NAME_SPACE);
    }



    @Resource
    protected CuratorFramework curatorFramework;

    protected   Logger log = LoggerFactory.getLogger(getClass());

    NodeCache nodeCache;

    protected String dataKey(){
        return nameSpace+getClass().getName();
    }

    protected abstract void renderCacheData(Obj cache);


    public int refresh() {


        if (isTest) {
            log.info(" isTest Env , Skip  Refresh ..");
            renderCacheData(null);
            return 0;
        }
        log.info(" Begin  Refresh ..");
        byte[] data = loadByteObjects(nodeCache.getCurrentData());
        Obj value =  deserialize(data);
        if(null != value){
            renderCacheData(value);
            return data.length;
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


    protected   Obj deserialize(byte[] data){
        if(null != data && data.length > 0){
            try (ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(data))){
                return (Obj)inp.readObject();
            } catch (Exception e) {
                log.error("deserialize byte[] Error",e);
            }
        }
        return null;
    }

    public static void setNameSpace(String given) {
        String ns = PREFIX;
        if(given.length()>0){
            ns += given +'/';
            nameSpace = ns;
            System.out.println("ZookeeperJavaMap use NameSpace =========> : "+ ns );

        }
    }
}
