package com.sktelecom.blockchain.byzantium.network.grpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.AbstractStub;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GRPCClient<STUB extends AbstractStub> {

    private final ManagedChannel channel;
    private final @Getter STUB stub;

    /**
     * 생성자
     * @param host
     * @param port
     * @param createStubFunction
     */
    public GRPCClient(String host, int port, Function<ManagedChannel, STUB> createStubFunction) {
        this.channel = NettyChannelBuilder
                .forAddress(host, port)
                .usePlaintext(true)
                .build();
        this.stub = createStubFunction.apply(this.channel);
    }

    /**
     * shutdown
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
