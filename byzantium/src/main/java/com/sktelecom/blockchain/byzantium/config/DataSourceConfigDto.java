package com.sktelecom.blockchain.byzantium.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DataSourceConfigDto {
    private String className;
    private String url;
    private String userName;
    private String password;

    private int maximumPoolSize;
    private int minimumIdleSize;
}
