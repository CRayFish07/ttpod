package com.ttpod.vertx;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 1. ONLY Support GET | POST ;
 * 2. ONLY Support Plan URI Path;
 *
 * 3. Path Variable  (eg. /abc/:var) Not Support.
 *
 * date: 14-2-24 上午11:45
 *
 * @author: yangyang.cong@ttpod.com
 */
public class SimpleRouteMatcher implements Handler<HttpServerRequest> {


    public static final String GET = "GET";
    public static final String POST = "POST";


    private final Map<String,Handler<HttpServerRequest>> getMap = new HashMap<>();
    private final Map<String,Handler<HttpServerRequest>> postMap = new HashMap<>();
    private final Map<String,Handler<HttpServerRequest>> allMap = new HashMap<>();

    @Override
    public void handle(HttpServerRequest request) {
        String method = request.method();
        String uri =  request.path().intern();
        Handler<HttpServerRequest> handler = null;
        
//        switch (method){
//            case GET:
//                handler = getMap.get(uri);
//                break;
//            case POST:
//                handler = postMap.get(uri);
//                break;
//        }
        if(GET.equals(method)){
            handler = getMap.get(uri);
        }else if (POST.equals(method)) {
            handler = postMap.get(uri);
        }

        if(null != handler){
            handler.handle(request);
        }else{
            handler = allMap.get(uri);
            if(null != handler){
                handler.handle(request);
            }else{
                request.response().setStatusCode(404);
                request.response().end("URI Not Found OR Request Method Not Support.");
            }
        }
    }


    public SimpleRouteMatcher get(String url,final Closure handler) {
        return get(url,new Handler<HttpServerRequest>(){
            public void handle(HttpServerRequest event) {
                handler.call(event);
            }
        });
    }

    public SimpleRouteMatcher get(String url,Handler<HttpServerRequest> handler) {
        getMap.put(url.intern(),handler);
        return this;
    }

    public SimpleRouteMatcher post(String url,final Closure handler) {
        return post(url, new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest event) {
                handler.call(event);
            }
        });
    }

    public SimpleRouteMatcher post(String url,Handler<HttpServerRequest> handler) {
        postMap.put(url.intern(),handler);
        return this;
    }

    public SimpleRouteMatcher uri(String url,final Closure handler) {
        return uri(url, new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest event) {
                handler.call(event);
            }
        });
    }

    public SimpleRouteMatcher uri(String url,Handler<HttpServerRequest> handler) {
        allMap.put(url.intern(),handler);
        return this;
    }

}
