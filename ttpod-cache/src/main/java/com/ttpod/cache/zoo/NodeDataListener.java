package com.ttpod.cache.zoo;

/**
 * Other classes use the DataMonitor by implementing this method
 */
public interface NodeDataListener {
    /**
     * The existence status of the node has changed.
     */
    void onDataChanged(byte data[]);

    /**
     * The ZooKeeper session is no longer valid.
     *
     * @param rc
     *                the ZooKeeper reason code
     */
//        void closing(int rc);
}
