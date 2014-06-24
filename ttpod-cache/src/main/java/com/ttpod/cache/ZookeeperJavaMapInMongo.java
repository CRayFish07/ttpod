package com.ttpod.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * date: 2014/5/22 14:46
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class ZookeeperJavaMapInMongo<K, V> extends ZookeeperJavaMap<K, V> {

    public static final String DB_NAME = "zookeeper";
    public static final String COLL_NAME = "java_map";

    public static final String DATA_FIELD = "data";
    public static final String DATAKEY_PATH = "path";
    public static final String SLICE_FIELD = "slice";


    @Resource
    protected Mongo mongo;

    protected byte[] transform(byte[] nodeData) {

        String dataKey = dataKey();
        log.debug("use dataKey : {}",dataKey);
        List<DBObject> byte_segs = mongo.getDB(dbName()).getCollection(collectionName()).find(
                new BasicDBObject(DATAKEY_PATH, dataKey), new BasicDBObject(DATA_FIELD, 1)
        ).sort(new BasicDBObject(SLICE_FIELD, 1)).toArray();

        if (byte_segs.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
        for (DBObject obj : byte_segs) {
            try {
                byte [] bytes = (byte []) obj.get(DATA_FIELD);
                out.write(bytes);
            } catch (IOException e) {
                log.error("read data from Mongo Error",e);
                return null;
            }
        }

        return out.toByteArray();
    }

    protected String collectionName() {
        return COLL_NAME;
    }
    protected String dbName() {
        return DB_NAME;
    }

}
