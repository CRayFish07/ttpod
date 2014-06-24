package test.zoo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;

/**
 *
 * http://wiki.apache.org/hadoop/ZooKeeper/FAQ
 * 4. Is there an easy way to expire a session for testing?
 * date: 14-6-23 17:09
 *
 * @author: yangyang.cong@ttpod.com
 */
public class TestSameSessionId {

    @Test
    public void testPwd() throws IOException, InterruptedException {
        ZooKeeper zoo = new ZooKeeper("192.168.8.12:2181",3000,new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    testPwd();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },0x146c7e7cdfb0012L,new byte[16]);
        Thread.currentThread().join();
    }
}
