package com.sktelecom.blockchain.byzantium.network.http;

import com.sktelecom.blockchain.byzantium.config.RawHttpClientConfigDto;
import lombok.Getter;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RawHttpClient {

    /** media type for json */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** http clients */
    private @Getter OkHttpClient httpClient;

    /**
     * constructor
     * @param option
     * @param interceptors
     */
    public RawHttpClient(RawHttpClientConfigDto option, Interceptor... interceptors) {

        // Connection Pool 생성
        ConnectionPool pool = new ConnectionPool(option.getMaxIdleConnections(), option.getKeepAliveDuration(), SECONDS);

        // http client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(option.getConnectTimeout(), SECONDS)
                .readTimeout(option.getReadTimeout(), SECONDS)
                .writeTimeout(option.getWriteTimeout(), SECONDS);

        // interceptor 추가
        Arrays.asList(interceptors).forEach(builder::addInterceptor);

        // http client 생성
        this.httpClient = builder.build();
    }

    /**
     * post
     * @param uri
     * @param json
     * @return
     * @throws IOException
     */
    public Response post(String uri, String json) throws IOException {
        return this.httpClient
                .newCall(new Request
                        .Builder()
                        .url(uri)
                        .post(RequestBody
                                .create(JSON, json))
                        .build())
                .execute();
    }
}
