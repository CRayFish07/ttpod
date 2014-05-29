package com.ttpod.cache;

/**
 *      缓存加载的服务类.
 *
 *      缓存的创建者和消费者进行隔离。
 *
 *      一处创建，多处消费。
 *
 *      中间通过Zookeeper进行通信。
 *
 *      创建者定时任务把序列化的缓存（Map）更新到Zookeeper中(或者Link),
 *   消费者自动收到Zookeeper的通知，自行处理更新。
 *
 *      通过Zookeeper解耦 创建者和消费者。
 *
 *      date: 13-10-30 上午11:13
 *      @author: yangyang.cong@ttpod.com
 */
public interface ICacheService<Key,Value> {

    int  refresh();

//    IdStrategy ID_STRATEGY = new DefaultIdStrategy();
//    Schema<HashMap> MAP_SCHEMA =  RuntimeSchema.getSchema(HashMap.class, ID_STRATEGY);


    Value get(Key key);



}