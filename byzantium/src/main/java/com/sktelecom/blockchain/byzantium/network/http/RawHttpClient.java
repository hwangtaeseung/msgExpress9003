package com.sktelecom.blockchain.byzantium.network.http;

import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method;
import lombok.Getter;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RawHttpClient {

    /** media type for json */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** http clients */
    private @Getter OkHttpClient httpClient;

    /** thread pool */
    private ExecutorService executorService;

    /**
     * constructor
     * @param config
     * @param interceptors
     */
    public RawHttpClient(HttpClientConfigDto config, Interceptor... interceptors) {

        // Connection Pool 생성
        ConnectionPool pool = new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), SECONDS);

        // create execute service
        this.executorService = Executors.newFixedThreadPool(config.getMaxIdleConnections());

        // http client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(config.getConnectTimeout(), SECONDS)
                .readTimeout(config.getReadTimeout(), SECONDS)
                .writeTimeout(config.getWriteTimeout(), SECONDS);

        // interceptor 추가
        Arrays.asList(interceptors).forEach(builder::addInterceptor);

        // http client 생성
        this.httpClient = builder.build();
    }

    /**
     * call rest api
     * @param method
     * @param uri
     * @param jsonBody
     * @return
     */
    public Response call(Method method, String uri, String jsonBody) throws IOException {

        Request.Builder builder = new Request.Builder().url(uri);
        RequestBody body = RequestBody.create(JSON, jsonBody);

        // build request by method
        switch (method) {
            case POST: {
                builder.post(body);
                break;
            }
            case PUT: {
                builder.put(body);
                break;
            }
            case DELETE: {
                builder.delete(body);
                break;
            }
        }

        // execute to call rest api
        return this.httpClient
                .newCall(builder.build())
                .execute();
    }

    /**
     * shutdown
     */
    public void shutdown() {
        this.executorService.shutdown();
    }
}
