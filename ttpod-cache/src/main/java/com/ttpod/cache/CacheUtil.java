package com.ttpod.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.bson.types.Binary;

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
    static final int _16MB = 16 * _1MB;


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
     * @see org.apache.zookeeper.Zookeeper#create
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
        new EnsurePath(path).ensure(zoo.getZookeeperClient());
        zoo.setData().forPath(path, String.valueOf(System.currentTimeMillis()).getBytes(Charset.forName("utf8")));

        coll.remove(new BasicDBObject(ZookeeperJavaMapInMongo.DATAKEY_PATH, path));// DO Clean
        coll.createIndex(new BasicDBObject(ZookeeperJavaMapInMongo.DATAKEY_PATH,1).append(ZookeeperJavaMapInMongo.SLICE_FIELD,1));

        int step = _16MB, len = data.length;
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
        }

    }

    /**
     *
     * @see #bigDataLinkToZoo(org.apache.curator.framework.CuratorFramework, String, com.mongodb.DBCollection, byte[])
     */
    public static void bigDataLinkToZoo(CuratorFramework zoo, String path, DBCollection coll, Serializable javaMap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(_16MB);
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