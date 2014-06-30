package com.ttpod.cache;

import com.ttpod.cache.transform.ITransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * date: 14-6-30 13:35
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class AbstractCacheAble<Obj>  implements ICacheAble<Obj>{


    protected Logger log = LoggerFactory.getLogger(getClass());

    static final boolean isTest = Boolean.getBoolean("env.test");

    static final String NAME_SPACE = System.getProperty("cache.ns","").trim();

    static final String PREFIX = "/JavaMap/";


    static String nameSpace = PREFIX;

    static {
        setNameSpace(NAME_SPACE);
    }

    public static void setNameSpace(String given) {
        String ns = PREFIX;
        if(given.length()>0){
            ns += given +'/';
            nameSpace = ns;
            System.out.println("ICacheAble use NameSpace =========> : "+ ns );

        }
    }



    protected String dataKey(){
        return nameSpace+getClass().getName();
    }

    public void setTransform(ITransform transform) {
        this.transform = transform;
    }

    protected ITransform transform = ITransform.DEFAULT;



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

    protected boolean needDesri = true;


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


    abstract public byte[] currentData();

    protected final AtomicBoolean isInit = new AtomicBoolean(false);

    @Override
    public void refresh(){

        if (isTest) {
            isInit.set(true);
            log.info(" isTest Env , Skip  Refresh ");
            renderCacheData(null);
            return;
        }

        log.info(" Begin  Refresh ..");
        byte[] dataTran = transform.transform( dataKey() ,currentData());
        Obj value;
        if (needDesri) {
            value = deserialize(dataTran);
        } else {
            value = (Obj) dataTran;
        }
        renderCacheData(value);
        if (null == value) {
            log.info(" Refresh End(Maybe Has Error) , got NULL.");
        }
        isInit.set(true);
    }

}
