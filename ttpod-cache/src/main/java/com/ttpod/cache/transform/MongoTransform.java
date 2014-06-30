package com.ttpod.cache.transform;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * date: 14-6-30 14:12
 *
 * @author: yangyang.cong@ttpod.com
 */
public class MongoTransform implements ITransform {


//    @Resource
    protected Mongo mongo;

    protected String dbName = DB_NAME;
    protected String collectionName = COLL_NAME;


    public static final String DB_NAME = "zookeeper";
    public static final String COLL_NAME = "java_map";

    public static final String DATA_FIELD = "data";
    public static final String DATAKEY_PATH = "path";
    public static final String SLICE_FIELD = "slice";




    public byte[] transform( String dataKey ,byte[] nodeData) {

        List<DBObject> byte_segs = mongo.getDB(dbName).getCollection(collectionName).find(
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
                e.printStackTrace();
                return null;
            }
        }

        return out.toByteArray();
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }


    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }


}
