package com.sktelecom.blockchain.byzantium.network.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class GRPCServer<SERVICE extends BindableService> {

    /** handler */
    private SERVICE service;

    /** gRPC Server */
    private @Getter Server server;

    /**
     * constructor
     * @param service
     */
    public GRPCServer(SERVICE service) {
        this.service = service;
    }

    /**
     * Server 실행
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    public GRPCServer<SERVICE> start(String host, int port) throws IOException {
        // server 생성 & 실행
        this.server = NettyServerBuilder
                .forAddress(new InetSocketAddress(host, port))
                .addService(this.service)
                .build()
                .start();

        log.info("gRPC Server has been started...");
        return this;
    }

    /**
     * 종료
     */
    public void stop() {
        if (this.server != null)
            this.server.shutdown();
    }

    /**
     * 대기
     * @throws InterruptedException
     */
    public GRPCServer<SERVICE> blockUtilShutdown() throws InterruptedException {
        if (this.server != null) {
            this.server.awaitTermination();
        }
        log.info("gRPC server has been terminated.");
        return this;
    }
}
