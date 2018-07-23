package com.sktelecom.blockchain.byzantium.config;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RawHttpClientConfigDto {
    /** time out */
    private int readTimeout;
    private int connectTimeout;
    private int writeTimeout;

    /** http client 갯수 */
    private int maxIdleConnections;

    /** keep alive */
    private int keepAliveDuration;
}
