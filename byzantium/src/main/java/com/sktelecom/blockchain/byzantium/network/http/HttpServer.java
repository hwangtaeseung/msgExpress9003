package com.sktelecom.blockchain.byzantium.network.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sktelecom.blockchain.byzantium.config.HttpServerConfigDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import spark.ExceptionHandler;
import spark.Route;
import spark.Service;

import java.util.ArrayList;
import java.util.List;

public class HttpServer {

    /** JSON parser */
    private static @Getter GsonBuilder builder = new GsonBuilder();

    /** configuration */
    private @Getter HttpServerConfigDto config;

    /** HTTP Handlers */
    private List<Handler> handlers = new ArrayList<>();

    /** HTTP Handlers */
    private List<ExHandler> exceptionHandlers = new ArrayList<>();

    /** Http Server instance */
    private Service http = Service.ignite();

    /**
     * 실행
     * @param configDto
     */
    @SuppressWarnings("unchecked")
    public HttpServer run(HttpServerConfigDto configDto) {

        this.config = configDto;

        // 정적파일 path 설정
        if (configDto.getDocRoot() != null) {
            this.http.staticFiles.location(configDto.getDocRoot());
        }

        // thread 설정
        this.http.threadPool(configDto.getMaxThreads(), configDto.getMinThreads(), configDto.getTimeout());

        // ip address / port 설정
        this.http.ipAddress(configDto.getHttpHost());
        this.http.port(configDto.getHttpPort());

        // Handler 등록
        this.http.path(configDto.getUrlPath(), () -> {

            // handler 등록
            this.handlers.forEach(this::convertHandler);

            // exception handler 등록
            this.exceptionHandlers.forEach(exHandler -> this.http.exception(exHandler.getException(), exHandler.getHandler()));

            // 404 error 처리
            this.http.notFound((request, response) -> {
                response.type("application/json");
                return "{ message : \"custom 404\" }";
            });

            // 500 error 처리
            this.http.internalServerError((request, response) -> {
                response.type("application/json");
                return "{ message : \"custom 500\" }";
            });
        });

        // 초기화 완료가지 대기...
        this.http.awaitInitialization();

        return this;
    }

    /**
     * Http Server 중지
     */
    public void shutdown() {
        this.http.stop();
    }

    /**
     * handler 등록
     * @param handler
     */
    private void convertHandler(Handler handler) {

        // JSON Parser
        Gson gson = new Gson();

        switch (handler.getMethod()) {
            case GET: {
                this.http.get(handler.getUri(), handler.getRoute(), gson::toJson);
                break;
            }
            case POST: {
                this.http.post(handler.getUri(), handler.getRoute(), gson::toJson);
                break;
            }
            case PUT: {
                this.http.put(handler.getUri(), handler.getRoute(), gson::toJson);
                break;
            }
            case DELETE: {
                this.http.delete(handler.getUri(), handler.getRoute(), gson::toJson);
                break;
            }
        }
    }

    /**
     * JSON String 을 객체로
     * @param json
     * @param responseObjectClass
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(String json, Class<T> responseObjectClass) {
        return builder.create().fromJson(json, responseObjectClass);
    }

    /**
     * 객체를 JSON String 으로
     * @param object
     * @param <T>
     * @return
     */
    public static <T> String objectToString(T object) {
        return builder.create().toJson(object);
    }

    public enum Method {
        GET(0), POST(1), PUT(2), DELETE(3);
        @Getter int value;
        Method(int value) {
            this.value = value;
        }
    }

    @Getter @AllArgsConstructor
    public static class Handler {
        Method method;
        String uri;
        Route route;
    }

    @Getter
    @AllArgsConstructor
    public static class ExHandler<E extends Exception> {
        Class<E> exception;
        ExceptionHandler<E> handler;
    }

    /**
     * Rest Handler 추가
     * @param method
     * @param uri
     * @param route
     * @return
     */
    public HttpServer addHandler(Method method, String uri, Route route) {
        this.handlers.add(new Handler(method, uri, route));
        return this;
    }

    /**
     * Handler 추가
     * @param handler
     * @return
     */
    public HttpServer addHandler(Handler handler) {
        this.handlers.add(handler);
        return this;
    }

    /**
     * 예외 Handler 추가
     * @param exceptionClass
     * @param handler
     * @param <E>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends Exception>HttpServer addExceptionHandler(Class<E> exceptionClass, ExceptionHandler<E> handler) {
        this.exceptionHandlers.add(new ExHandler(exceptionClass, handler));
        return this;
    }
}
