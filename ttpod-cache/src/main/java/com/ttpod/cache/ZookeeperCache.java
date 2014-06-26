package com.ttpod.cache;

import com.ttpod.cache.zoo.NodeDataCache;
import com.ttpod.cache.zoo.NodeDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.ObjectInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 定时任务把计算结果存到zookeeper中
 *
 * 客户机自动从zookeeper获取更新.
 *
 * ZooKeeper Node limit 1 Mb.
 *
 * If node Data > 1Mb. pls save in Mongo.
 * @see ZookeeperJavaMapInMongo
 *
 *
 * date: 2014/5/5 10:23
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class ZookeeperCache<Obj> implements Closeable,NodeDataListener{


    static final boolean isTest = Boolean.getBoolean("env.test");

    static final String NAME_SPACE = System.getProperty("cache.ns","").trim();

    static final String PREFIX = "/JavaMap/";


    static String nameSpace = PREFIX;

    static {
        setNameSpace(NAME_SPACE);
    }



    @Resource
    protected String  zookeeperUrl;

    protected   Logger log = LoggerFactory.getLogger(getClass());


    protected String dataKey(){
        return nameSpace+getClass().getName();
    }

    protected abstract void renderCacheData(Obj cache);

    NodeDataCache nodeDataCache;


    protected boolean needDesri = true;



    final AtomicBoolean isInit =  new AtomicBoolean(false);
    @PostConstruct
    protected void init(){
        nodeDataCache = new NodeDataCache(zookeeperUrl, dataKey(),this);
        log.info("Wait Init ...");
        int i = 0;
        while (!isInit.get() && (++i) < 60){
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

        if (isTest) {
            isInit.set(true);
            log.info(" isTest Env , Skip  Refresh from {}..",zookeeperUrl);
            renderCacheData(null);
            return;
        }

        log.info(" Begin  Refresh ..");
        byte[] dataTran = transform(data);
        Obj value;
        if(needDesri) {
            value = deserialize(dataTran);
        }else{
            value = (Obj) dataTran;
        }
        renderCacheData(value);
        if(null == value){
            log.info(" Refresh End(Error) , got NULL.");
        }
        isInit.set(true);
    }
    @PreDestroy
    public void close(){
        if(null!=nodeDataCache)nodeDataCache.close();
    }

    protected byte[] transform(byte[] nodeData){
        return nodeData;
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

    {
        Type[]  typeParameters = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments();
        if( typeParameters.length == 0 ){
            typeParameters = ((ParameterizedType)getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments();
        }
        if((typeParameters.length == 1) && (byte[].class == typeParameters[0])){
            needDesri = false;
            log.info("byte[] not need to deserialize....");
        }
    }
}
