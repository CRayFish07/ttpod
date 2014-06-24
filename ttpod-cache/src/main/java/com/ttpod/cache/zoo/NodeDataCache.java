package com.ttpod.cache.zoo;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;

/**
 * date: 14-6-23 14:24
 *
 * @author: yangyang.cong@ttpod.com
 */
public class NodeDataCache implements  Runnable,Closeable{

    static final Logger log = LoggerFactory.getLogger(NodeDataCache.class);

    final int TIME_OUT = 3000;

    final DataMonitor dm;
    ZooKeeper zk;
    private String zooUrl;



    public NodeDataCache(String zooUrl, String znode, NodeDataListener listener) {
        this.zooUrl = zooUrl;
        dm = new DataMonitor(znode,listener,this);
        run();
    }



    long delay = 1000L;
    static final long maxDelay = 60 * 1000L;
//    Semaphore semaphore = new Semaphore(1);
    public synchronized void run(){
        if( dm.needReInit ){
            while (true) {
                try {

                    if (null != zk) {
                        zk.close();
                        zk = null;
                        log.info(Thread.currentThread().getId() + "close old Zookeeper client ");
                        delay = Math.min(delay * 2, maxDelay);
                        log.info("connTo {} error , sleep {} ms To Continue.", zooUrl, delay);
                        Thread.sleep(delay);
                    }

                    zk = new ZooKeeper(zooUrl, TIME_OUT, dm);//, 9527L, "9527".getBytes());
                    dm.init(zk);
//                    semaphore.release();
                    break;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    @PostConstruct
    public void close(){
        if(null != zk){
            log.info("shutdown NodeCahe {}/{}",zooUrl,dm.znode);
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
