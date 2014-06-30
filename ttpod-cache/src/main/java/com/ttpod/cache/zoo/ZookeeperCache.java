package com.ttpod.cache.zoo;

import com.ttpod.cache.AbstractCacheAble;
import com.ttpod.cache.zoo.NodeDataCache;
import com.ttpod.cache.zoo.NodeDataListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Closeable;

/**
 * 定时任务把计算结果存到zookeeper中
 * <p/>
 * 客户机自动从zookeeper获取更新.
 * <p/>
 * ZooKeeper Node limit 1 Mb.
 * <p/>
 * If node Data > 1Mb. pls save in Mongo.
 *
 * @author: yangyang.cong@ttpod.com
 * <p/>
 * <p/>
 * date: 2014/5/5 10:23
 */
public abstract class ZookeeperCache<Obj> extends AbstractCacheAble<Obj> implements Closeable, NodeDataListener {


    @Resource
    protected String zookeeperUrl;


    NodeDataCache nodeDataCache;


    @PostConstruct
    public void init() {
        nodeDataCache = new NodeDataCache(zookeeperUrl, dataKey(), this);
        log.info("Wait Init ...");
        int i = 0;
        while (!isInit.get() && (++i) < 60) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Init Over !");

    }

    @Override
    public void onDataChanged(byte[] data) {
        refresh();
    }

    @PreDestroy
    public void close() {
        if (null != nodeDataCache) nodeDataCache.close();
    }


    public byte[] currentData() {
        return nodeDataCache.currentData();
    }


}
