package com.ttpod.cache.zoo;
/**
 * A simple class that monitors the data and existence of a ZooKeeper
 * node. It uses asynchronous ZooKeeper APIs.
 */

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.zip.CRC32;


class DataMonitor implements Watcher, StatCallback {

    ZooKeeper zk;

    final String znode;

//    Watcher chainedWatcher;

    boolean needReInit = true;

    NodeDataListener listener;

    Long prevDataHash;

    Runnable onClosing;

    final Executor exe;
    public DataMonitor(String znode,NodeDataListener listener,Runnable onClosing,Executor exe) {
        this.znode = znode;
        this.listener = listener;
        this.onClosing = onClosing;
        this.exe =exe;
    }

    public void init( ZooKeeper zk){
        this.zk = zk;
        this.needReInit = false;
        // Get things started by checking if the node onDataChanged. We are going
        // to be completely event driven
        zk.exists(znode, true, this, null);
    }


    static final Logger log  = LoggerFactory.getLogger(DataMonitor.class);



    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None) {
            log.info("connection has changed , with status : {} ",event.getState());
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    needReInit = true;
    //                listener.closing(Code.SESSIONEXPIRED.intValue());
                    exe.execute(onClosing);
                    break;
            }
        } else  if ( znode.equals(event.getPath()) ) {
                // Something has changed on the node, let's find out
                zk.exists(znode, true, this, null);
        }
    }

    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
        case Code.Ok:
            exists = true;
            break;
        case Code.NoNode:
            exists = false;
            break;
        case Code.SessionExpired:
        case Code.NoAuth:
            needReInit = true;
//            listener.closing(rc);
            exe.execute(onClosing);
            return;
        default:
            // Retry errors
            zk.exists(znode, true, this, null);
            return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        Long hash = hash(b);
        if ((b == null && null != prevDataHash)
                || (b != null && !hash.equals(prevDataHash))) {
            final byte[] data = b;
            exe.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onDataChanged(data);
                }
            });
            prevDataHash = hash;
        }
    }


    Long hash(byte[] data){
        if(null == data){
            return null;
        }
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
        //return  Long.toHexString(crc.getValue());
    }
}