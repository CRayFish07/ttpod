package com.ttpod.cache.redis;

import com.ttpod.cache.AbstractCacheAble;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 *
 *  定时任务把计算结果存到redis 中
 *
 * <p/>
 * 客户机需要得到通知获取更新.
 *
 * date: 14-6-30 16:05
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class RedisCache<Obj> extends AbstractCacheAble<Obj> {


    /**
     * 8-bit UTF (UCS Transformation Format)
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");


    @Resource
    Jedis jedis;

    @Override
    public byte[] currentData() {
        return jedis.get(dataKey().getBytes(UTF_8));
    }

    @Override
    @PostConstruct
    public void refresh() {
        super.refresh();
    }
}
