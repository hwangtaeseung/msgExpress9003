package com.sktelecom.blockchain.byzantium.application;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class AppJsonConfiguration {

    /**
     * resource 로 부터 환경 파일 로딩
     * @param fileName
     * @param configDtoClass
     * @param <CONFIG>
     * @return
     * @throws Exception
     */
    public static <CONFIG> CONFIG loadConfig(String fileName, Class<CONFIG> configDtoClass) throws Exception {

        CONFIG configDto;
        try (InputStream is = configDtoClass.getClass().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            // parse json
            configDto = new Gson().fromJson(reader, configDtoClass);

        } catch (IOException e) {
            log.error("config loading error.. {}", fileName, e);
            throw e;
        }

        return configDto;
    }
}
