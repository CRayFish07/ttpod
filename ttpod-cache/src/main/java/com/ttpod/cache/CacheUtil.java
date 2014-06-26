package com.ttpod.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * date: 2014/5/29 13:53
 *
 * @author: yangyang.cong@ttpod.com
 */
public  abstract class CacheUtil {


    static final int _1MB = 1 << 20;
    static final int _12MB = 12 * _1MB; // mongo MAX Size 16777501 is larger than MaxDocumentSize 16793600.

    static final Logger log = LoggerFactory.getLogger(CacheUtil.class);


    /**
     * default Zookeeper Limit Data Node < 1MB.
     * <p/>
     * The maximum allowable size of the data array is 1 MB (1,048,576 bytes).
     * Arrays larger than this will cause a KeeperExecption to be thrown.
     *
     * @param zoo     CuratorFramework
     * @param path    zookeeper path
     * @param javaMap object impl Serializable
     * @throws Exception
     * @link org.apache.zookeeper.Zookeeper#create
     */
    public static void smallDataToZoo(CuratorFramework zoo, String path, Serializable javaMap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(_1MB);
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(javaMap);
            out.flush();
            smallDataToZoo(zoo, path, bos.toByteArray());
        }
    }

    /**
     * @see #smallDataToZoo(org.apache.curator.framework.CuratorFramework, String, java.io.Serializable)
     */
    public static void smallDataToZoo(CuratorFramework zoo, String path, byte[] data) throws Exception {
        new EnsurePath(path).ensure(zoo.getZookeeperClient());
        zoo.setData().forPath(path, data);
    }


    /**
     *
     * if data.length > 1 MB (1,048,576 bytes), PLS save to MongoDb, just save a Link In Zookeeper.
     *
     * convention over configuration, so the link is just a timestamp in zookeeper.
     * @throws Exception
     */
    public static void bigDataLinkToZoo(CuratorFramework zoo, String path, DBCollection coll, byte[] data) throws Exception {

        DBObject query = new BasicDBObject(ZookeeperJavaMapInMongo.DATAKEY_PATH, path);
        coll.remove(query);// DO Clean
        Thread.sleep(500L);// wait mongo sync
        coll.createIndex(new BasicDBObject(ZookeeperJavaMapInMongo.DATAKEY_PATH,1).append(ZookeeperJavaMapInMongo.SLICE_FIELD,1));

        int step = _12MB, len = data.length;
        int i = 0;
        for (int pos = 0; pos < len; pos += step) {
            int copy = Math.min(len - pos, step);
            byte[] slice = new byte[copy];
            System.arraycopy(data, pos, slice, 0, copy);
            BasicDBObject obj = new BasicDBObject();
            obj.put("_id", path + i);
            obj.put(ZookeeperJavaMapInMongo.DATA_FIELD, new Binary(slice));
            obj.put(ZookeeperJavaMapInMongo.SLICE_FIELD, i++);
            obj.put(ZookeeperJavaMapInMongo.DATAKEY_PATH, path);
            obj.put("slice_len", slice.length);

            coll.save(obj);
            Thread.sleep(100L);//wait mongo sync
            // TODO Restart ERROR during insert Not Complete.
        }

        Thread.sleep(500L);
        while (coll.count(query) != i){
            log.info("wait mongo sync with master.sleep 300ms.");
            Thread.sleep(300);
        }

        new EnsurePath(path).ensure(zoo.getZookeeperClient());//Notify..
        zoo.setData().forPath(path, String.valueOf(System.currentTimeMillis()).getBytes(Charset.forName("utf8")));

    }

    /**
     *
     * @see #bigDataLinkToZoo(org.apache.curator.framework.CuratorFramework, String, com.mongodb.DBCollection, byte[])
     */
    public static void bigDataLinkToZoo(CuratorFramework zoo, String path, DBCollection coll, Serializable javaMap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(_12MB);
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(javaMap);
            out.flush();
            bigDataLinkToZoo(zoo, path, coll, bos.toByteArray());
        }
    }
    /**
     *
     * @see #bigDataLinkToZoo(org.apache.curator.framework.CuratorFramework, String, com.mongodb.DBCollection, byte[])
     */
    public static void bigDataLinkToZoo(CuratorFramework zoo, String path, Mongo mongo, Serializable javaMap) throws Exception {
        bigDataLinkToZoo(zoo, path, mongo.getDB(ZookeeperJavaMapInMongo.DB_NAME)
                .getCollection(ZookeeperJavaMapInMongo.COLL_NAME),javaMap);
    }
    /**
     *
     * @see #bigDataLinkToZoo(org.apache.curator.framework.CuratorFramework, String, com.mongodb.DBCollection, byte[])
     */
    public static void bigDataLinkToZoo(CuratorFramework zoo, String path, Mongo mongo, byte[] javaMap) throws Exception {
        bigDataLinkToZoo(zoo, path, mongo.getDB(ZookeeperJavaMapInMongo.DB_NAME)
                .getCollection(ZookeeperJavaMapInMongo.COLL_NAME),javaMap);
    }

}