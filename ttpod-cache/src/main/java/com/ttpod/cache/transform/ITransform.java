package com.ttpod.cache.transform;

/**
 * date: 14-6-30 14:11
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ITransform {

    byte[] transform( String key ,byte[] nodeData) ;


    ITransform DEFAULT = new ITransform() {
        @Override
        public byte[] transform(String key, byte[] nodeData) {
            return nodeData;
        }
    };

}
