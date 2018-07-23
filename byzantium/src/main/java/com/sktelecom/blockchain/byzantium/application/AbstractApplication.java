package com.sktelecom.blockchain.byzantium.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractApplication<CONFIG> {

    /** Application configuration object */
    private @Getter CONFIG config;

    /**
     * constructor
     * @param config
     */
    public AbstractApplication(CONFIG config) {
        // 환경설정 loading
        this.config = config;
    }

    /**
     * execution logic
     * @throws Exception
     */
    protected abstract void run(CONFIG config) throws Exception;

    /**
     * shutdown logic
     * @throws Exception
     */
    protected abstract void shutdown(CONFIG config) throws Exception;

    /**
     * Application 실행
     * @throws Exception
     */
    public void execute() throws Exception {

        // shutdown callback 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // shutdown callback 실행
            try {
                this.shutdown(this.config);

            } catch (Exception ignored) { }
        }));

        // application 실행
        this.run(this.config);
    }
}
