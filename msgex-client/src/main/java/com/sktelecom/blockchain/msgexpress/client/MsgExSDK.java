package com.sktelecom.blockchain.msgexpress.client;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.network.grpc.GRPCClient;
import com.sktelecom.blockchain.byzantium.network.http.HttpServer;
import com.sktelecom.blockchain.byzantium.utilities.TimeUtils;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.ProduceServerGrpc;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.ProduceServerGrpc.ProduceServerFutureStub;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Message Express SDK
 */
@Slf4j
public class MsgExSDK {

    /** JSON parser */
    private static Gson gson = new Gson();

    /** grpc client */
    private GRPCClient<ProduceServerFutureStub> grpcClient;

    /** work thread pool */
    private ExecutorService executorService;

    /** the count to retry to connect */
    private int retryCount = 3;

    /** api timeout */
    private int timeout = 5;

    /**
     * constructor
     * @param host
     * @param port
     */
    public MsgExSDK(String host, int port, int countOfWorkThread) {

        // create grpc client
        this.grpcClient = new GRPCClient<>(host, port, ProduceServerGrpc::newFutureStub);

        // create executeService
        this.executorService = Executors.newFixedThreadPool(countOfWorkThread);
    }

    /**
     * call API
     * @param host
     * @param port
     * @param method
     * @param apiName
     * @param dto
     * @param receiveCallback
     * @param <DTO>
     */
    public <DTO> MsgExSDK callApi(String host, int port, HttpServer.Method method, String apiName, boolean needToReply, DTO dto, Runnable receiveCallback) {
        // execute rpc by grpc client
        this.grpcClient
                .getStub()
                .sendMessage(getSendMessageRequest(host, port, method, apiName, needToReply, dto))
                .addListener(receiveCallback, this.executorService);

        return this;
    }

    /**
     * call API
     * @param host
     * @param port
     * @param method
     * @param apiName
     * @param needToReply
     * @param dto
     * @param <DTO>
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public <DTO> sendMessageResponse callApi(String host, int port, HttpServer.Method method, String apiName, boolean needToReply, DTO dto) throws ExecutionException, InterruptedException {
        return this.grpcClient.getStub().sendMessage(getSendMessageRequest(host, port, method, apiName, needToReply, dto)).get();
    }

    /**
     * create sendMessageRequest
     * @param host
     * @param port
     * @param method
     * @param apiName
     * @param needToReply
     * @param dto
     * @param <DTO>
     * @return
     */
    private <DTO> sendMessageRequest getSendMessageRequest(String host, int port, HttpServer.Method method, String apiName, boolean needToReply, DTO dto) {
        return sendMessageRequest.newBuilder()
                .setHeader(Basicmessage.MessageHeader.newBuilder()
                        .setMsgId(messageID())
                        .setMsgType(Basicmessage.MsgType.REQUEST)
                        .setTimestamp(TimeUtils.getUnixTimeStamp())
                        .build())
                .setDestinationAPI(Basicmessage.RestAPI.newBuilder()
                        .setHost(host)
                        .setPort(port)
                        .setMethod(Basicmessage.Method.values()[method.getValue()])
                        .setRestApi(apiName)
                        .setJsonBody(gson.toJson(dto))
                        .build())
                .setNeedToReply(needToReply)
                .setRetryCount(this.retryCount)
                .setTimeout(this.timeout)
                .build();
    }

    /**
     * shutdown
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        this.grpcClient.shutdown();
    }

    /**
     * create message id (global unique)
     * @return
     */
    private static String messageID() {
        return UUID.randomUUID().toString();
    }
}