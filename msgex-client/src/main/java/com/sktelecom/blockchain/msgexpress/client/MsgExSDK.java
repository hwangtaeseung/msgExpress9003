package com.sktelecom.blockchain.msgexpress.client;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.config.HttpServerConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.HttpServer;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExRestAPI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method.POST;
import static com.sktelecom.blockchain.byzantium.utilities.TimeUtils.getUnixTimeStamp;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.CLIENT_MESSAGE_RECEIVE_URI;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto.MsgType.*;
import static javax.servlet.http.HttpServletResponse.*;

/**
 * Message Express SDK
 */
@Slf4j
public class MsgExSDK {

    /** HTTP Server (to receive Asynchronous Response) */
    private @Getter HttpServer httpServer;

    /** transaction manager */
    private MsgExTransactionManager transactionManager;

    /** HTTP Client */
    private @Getter MsgExHttpClient httpClient;

    /** JSON parser */
    private static Gson gson = new Gson();

    /**
     * constructor
     * @param serverConfig
     * @param clientConfig
     * @param numOfPartition
     */
    public MsgExSDK(HttpServerConfigDto serverConfig, HttpClientConfigDto clientConfig, int numOfPartition, HttpServer.Handler... handlers) {

        // create transaction manager
        this.transactionManager = new MsgExTransactionManager(numOfPartition);

        // create http server
        this.httpServer = new HttpServer()

                // response receiver
                .addHandler(POST, CLIENT_MESSAGE_RECEIVE_URI + MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK, (message, response) -> {

                    // message id
                    String msgId = message.params(MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK);

                    // json parsing
                    MsgExMessageDto messageDto = gson.fromJson(message.body(), MsgExMessageDto.class);

                    // execute transaction callback
                    return MsgExResponseDto
                            .builder()
                            .header(MsgExHeaderDto
                                    .builder()
                                    .msgId(messageDto.getHeader().getMsgId())
                                    .msgType(BUS_RESPONSE)
                                    .timestamp(getUnixTimeStamp())
                                    .senderIP(this.httpServer.getConfig().getHttpHost())
                                    .senderTag("MicroService")
                                    .build())
                            .httpCode(
                                    this.transactionManager
                                            .pop(msgId)
                                            .orElse(this.transactionManager.getDefaultTransactionCallback())
                                            .apply(messageDto, null))
                            .message("transaction not found")
                            .build();
                });

        // add handlers
        Arrays.asList(handlers).forEach(this.httpServer::addHandler);

        // execute http server
        this.httpServer.run(serverConfig);

        // create http client
        this.httpClient = new MsgExHttpClient(clientConfig);
    }

    /**
     * shutdown
     */
    public void shutdown() {
        this.httpServer.shutdown();
    }


    /**
     * call api
     * @param receiveHost
     * @param receivePort
     * @param restAPI
     * @param transactionCallback
     * @return
     */
    public void call(String receiveHost, int receivePort, MsgExRestAPI restAPI, MsgExTransactionManager.Callback transactionCallback) {
        call(receiveHost, receivePort, restAPI, true, transactionCallback);
    }

    /**
     * call api
     * @param receiveHost
     * @param receivePort
     * @param restAPI
     * @param needToReply
     * @param transactionCallback
     * @return
     */
    public void call(String receiveHost, int receivePort, MsgExRestAPI restAPI, boolean needToReply, MsgExTransactionManager.Callback transactionCallback) {

        // work thread 로 실행
        this.httpClient.getExecutorService().execute(() -> {

            // create message id
            String messageId = messageID();

            // convert payload from  DTO to String
            MsgExMessageDto message = MsgExMessageDto
                    .builder()
                    .header(MsgExHeaderDto
                            .builder()
                            .msgId(messageId)
                            .msgType(API_REQUEST)
                            .timestamp(getUnixTimeStamp())
                            .senderIP(receiveHost)
                            .senderTag("SDK-REQUEST")
                            .build())
                    .destinationAPI(restAPI)
                    .receivePort(receivePort)
                    .needToReply(needToReply)
                    .build();

            // push transaction callback
            this.transactionManager.push(messageId, transactionCallback);

            // send response message
            transferMessage(message,

                    // execution callback
                    msg -> {
                        Response<MsgExResponseDto> response = httpClient.getService().transferMessage(messageId, msg).execute();

                        log.debug("SDK return value => httpCode={}, body={}", response.code(), response.body());

                        return response;
                    },

                    // exception callback
                    exception -> {
                        // pop transaction
                        this.transactionManager
                                .pop(messageId)
                                .ifPresent(callback -> callback.apply(null, exception));
                    });
        });

    }

    /**
     * reply response that related to call
     * @param message
     * @param payload
     * @param payloadClass
     * @param <DTO>
     * @return
     */
    public static <DTO> void reply(MsgExMessageDto message, DTO payload, Class<DTO> payloadClass, Callback<MsgExMessageDto, MsgExResponseDto> callback, ExecutorService executorService) {
        reply(message, gson.toJson(payload, payloadClass), callback, executorService);
    }

    /**
     * reply MsgEX Message
     * @param receivedMessage
     * @param payload
     */
    public static void reply(MsgExMessageDto receivedMessage, String payload, Callback<MsgExMessageDto, MsgExResponseDto> callback, ExecutorService executorService) {

        // work thread 로 실행
        executorService.execute(() -> {

            log.debug("--- in consumer ===> {}", receivedMessage.getHeader());

            // message ID
            String messageId = receivedMessage.getHeader().getMsgId();

            // create msgex message to reply
            MsgExMessageDto message = MsgExMessageDto
                    .builder()
                    .header(MsgExHeaderDto
                            .builder()
                            .msgId(messageId)
                            .msgType(API_RESPONSE)
                            .timestamp(getUnixTimeStamp())
                            .senderIP(receivedMessage.getHeader().getSenderIP())
                            .senderTag("SDK-RESPONSE")
                            .build())
                    // caller's rest api information to receive response message
                    .destinationAPI(MsgExRestAPI
                            .builder()
                            .host(receivedMessage.getHeader().getSenderIP())
                            .port(receivedMessage.getReceivePort())
                            .api(CLIENT_MESSAGE_RECEIVE_URI + MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK)
                            .method(POST)
                            .jsonBody(payload)
                            .build())
                    .receivePort(0)
                    .needToReply(false)
                    .build();

            // transfer response message
            transferMessage(message, callback, null);
        });
    }

    /**
     * message transfer
     * @param messageDto
     *
     */
    private static <T, R> void transferMessage(T messageDto, Callback<T, R> callback, Consumer<Exception> exceptionCallback) {

        try {
            // send message to message express
            retrofit2.Response response = callback.run(messageDto);

            // check result HTTP
            switch (response.code()) {
                case SC_OK:
                case SC_CREATED:
                case SC_ACCEPTED:
                {
                    log.debug("message express response (httpCode={}, body={})",
                            response.code(), response.body());
                    break;
                }
                // error
                default:
                {
                    log.error("send fail (httpCode={}, body={})", response.code(), response.body());
                    throw new Exception("{ httpError : " + response.code() + " }");
                }
            }

        } catch (Exception e) {
            log.error("message transfer error", e);
            if (exceptionCallback != null) exceptionCallback.accept(e);
        }
    }

    /**
     * create message id (global unique)
     * @return
     */
    private static String messageID() {
        return UUID.randomUUID().toString();
    }

    /**
     * SDK Callback
     * @param <T>
     * @param <R>
     */
    public interface Callback<T, R> {
        Response<R> run(T messageDto) throws Exception;
    }
}