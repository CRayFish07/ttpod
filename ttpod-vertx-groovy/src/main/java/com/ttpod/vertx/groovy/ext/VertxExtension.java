package com.ttpod.vertx.groovy.ext;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.lang.Object;import java.lang.String;import java.util.Map;

/**
 * date: 14-1-26 下午5:38
 *
 * @author: yangyang.cong@ttpod.com
 */
public class VertxExtension {

    public static void deployModule(Container container,String name, Map<String,Object> config) {
        container.deployModule(name,new JsonObject(config));
    }


    public static void get(RouteMatcher route,String path,final Closure hanlder) {
        route.get(path,new Handler<HttpServerRequest>(){
            public void handle(HttpServerRequest event) {
                hanlder.call(event);
            }
        });
    }


    public static void send (EventBus bus,String address, Map<String,Object> msg,final Closure hanlder){
        bus.send(address,new JsonObject(msg),new Handler<Message>() {
            public void handle(Message event) {
                hanlder.call(event);
            }
        });
    }

}