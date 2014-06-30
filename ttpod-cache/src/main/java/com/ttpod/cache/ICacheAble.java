package com.ttpod.cache;

/**
 * date: 14-6-30 11:56
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ICacheAble<Obj> {


    void renderCacheData(Obj cache);


    /**
     * 手动触发缓存更新
     */
    void refresh();


}
