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
public class HttpClient<SERVICE> extends RawHttpClient {

    /** service **/
    private @Getter SERVICE service;

    /**
     * 생성자
     * @param config
     */
    public HttpClient(HttpClientConfigDto config, Class<SERVICE> serviceClass, Interceptor... interceptors) {

        super(config, interceptors);

        // retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%d%s", config.getHost(), config.getPort(), config.getBaseUrl()))
                .client(this.getHttpClient())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .build();

        // service
        this.service = retrofit.create(serviceClass);
    }
}
