package com.sktelecom.blockchain.byzantium.config;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class HttpClientConfigDto {

    /** 기본 재시도 횟수 */
    private static final int DEFAULT_RETRY_COUNT = 3;

    /** 재시도시 대기 시간 */
    private static final long DEFAULT_RETRY_DELAY_TIME = 2000;

    /** Http Host */
    private String host;

    /** Http Port */
    private int port;

    /** server base url */
    private String baseUrl;

    /** time out */
    private int readTimeout;
    private int connectTimeout;
    private int writeTimeout;

    /** http client 갯수 */
    private int maxIdleConnections;

    /** keep alive */
    private int keepAliveDuration;
}
