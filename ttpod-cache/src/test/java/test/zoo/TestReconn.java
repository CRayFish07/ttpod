package test.zoo;

import com.ttpod.cache.zoo.NodeDataCache;
import com.ttpod.cache.zoo.NodeDataListener;
import org.junit.Test;

/**
 * date: 14-6-23 16:18
 *
 * @author: yangyang.cong@ttpod.com
 */
public class TestReconn {


    @Test
    public void testConn() throws InterruptedException {

        NodeDataCache rz = new NodeDataCache("192.168.8.12:2181","/test2013",new NodeDataListener() {
            @Override
            public void onDataChanged(byte[] data) {
                System.out.println("recevie : " + (data == null ? null : new String(data)));
            }
        });
        Thread.currentThread().join();


    }
}
