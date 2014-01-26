package com.ttpod.vertx.groovy.ext;

/**
 * date: 14-1-26 下午5:37
 *
 * @author: yangyang.cong@ttpod.com
 */
public final   class StaticExtension {
    public static long unixTime(System selfType) {
        return System.currentTimeMillis()/1000;
    }

    public static long currentSeconds(System selfType) {
        return System.currentTimeMillis()/1000;
    }
}
