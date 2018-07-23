package com.sktelecom.blockchain.byzantium.network.http;

import com.google.gson.GsonBuilder;
import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class HttpClient<SERVICE> {

    /** service **/
    private @Getter SERVICE service;

    /**
     * 생성자
     * @param config
     */
    public HttpClient(HttpClientConfigDto config, Class<SERVICE> serviceClass, Interceptor... interceptors) {

        // Connection Pool 생성
        ConnectionPool pool = new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), SECONDS);

        // http client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(config.getConnectTimeout(), SECONDS)
                .readTimeout(config.getReadTimeout(), SECONDS)
                .writeTimeout(config.getWriteTimeout(), SECONDS);

        // interceptor 추가
        Arrays.asList(interceptors).forEach(builder::addInterceptor);

        // http client 생성
        OkHttpClient httpClient = builder.build();

        // retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%d%s", config.getHost(), config.getPort(), config.getBaseUrl()))
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .build();

        // service
        this.service = retrofit.create(serviceClass);
    }
}
