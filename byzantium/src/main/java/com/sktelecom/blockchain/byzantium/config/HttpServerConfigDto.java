package com.sktelecom.blockchain.byzantium.config;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class HttpServerConfigDto {

    // WAS 설정
    private String httpHost;
    private int httpPort;
    private String docRoot;
    private String urlPath;

    // Thread 설정
    private int maxThreads;
    private int minThreads;
    private int timeout;
}
