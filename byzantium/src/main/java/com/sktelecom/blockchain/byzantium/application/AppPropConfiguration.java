package com.sktelecom.blockchain.byzantium.application;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties 파일로 된 환경화일 로딩
 */
@Slf4j
public class AppPropConfiguration {

    /**
     * load configuration file
     * @param fileName
     * @return
     */
    public static Properties loadConfig(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream is = properties.getClass().getResourceAsStream(fileName)) {
            properties.load(is);

        } catch (IOException e) {
            log.error("config loading error.. {}", fileName, e);
           throw e;
        }
        return properties;
    }
}
