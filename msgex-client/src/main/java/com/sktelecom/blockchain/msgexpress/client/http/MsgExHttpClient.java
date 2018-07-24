package com.sktelecom.blockchain.msgexpress.client.http;

import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.HttpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Msg Express Http Client
 */
@Slf4j
public class MsgExHttpClient extends HttpClient<MsgExProducerHttpService> {

    /** executor service for work thread */
    private @Getter ExecutorService executorService;

    /**
     * 생성자
     * @param config
     * @param interceptors
     */
    public MsgExHttpClient(HttpClientConfigDto config, Interceptor... interceptors) {
        super(config, MsgExProducerHttpService.class, interceptors);
        // create work thread pool
        this.executorService = Executors.newFixedThreadPool(config.getMaxIdleConnections());
    }
}
