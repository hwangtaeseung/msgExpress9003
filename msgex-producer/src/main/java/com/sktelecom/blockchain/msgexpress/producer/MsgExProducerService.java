package com.sktelecom.blockchain.msgexpress.producer;


import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.queue.kafka.KProducer;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.ProduceServerGrpc;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExAdaptor;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.ResponseSystem.KAFKA;
import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.Result.FAILURE;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.DEFAULT_TOPIC;
import static java.lang.Integer.parseInt;

/**
 * handler to process to send gRPC Message to kafka
 */
@Slf4j
public class MsgExProducerService extends ProduceServerGrpc.ProduceServerImplBase {

    /** kafka producer */
    private KProducer<String, String> producer;

    /** topics */
    private String topics;

    /** json parser */
    private static Gson gson = new Gson();

    /** transaction manager */
    private MsgExTransactionManager transactionManager;

    /**
     * constructor
     * @param producer
     */
    MsgExProducerService(KProducer<String, String> producer, MsgExTransactionManager transactionManager, Properties properties) {
        this.producer = producer;
        this.topics = properties.getProperty("msgex.producer.send_topics", DEFAULT_TOPIC + "-send");
        this.transactionManager = transactionManager;
    }

    /**
     * receive handler for sendMessage()
     * @param request
     * @param responseObserver
     */
    @Override
    public void sendMessage(sendMessageRequest request, StreamObserver<sendMessageResponse> responseObserver) {

        log.debug("receive 'sendMessage' message : msgId={}, msgtype={}, topic={}, jsonBody={}",
                request.getHeader().getMsgId(),
                request.getHeader().getMsgType(),
                request.getHeader().getTopicName(),
                request.getRestAPI().getJsonBody());

        // add transaction
        this.transactionManager.push(request.getHeader().getMsgId(), responseObserver);

        // send msg to kafka
        this.producer.send(this.topics != null ? this.topics : request.getHeader().getTopicName(),

                // key
                request.getHeader().getMsgId(),

                // value
                gson.toJson(MsgExAdaptor.toMsgExMessage(request)),

                // kafka callback
                (metadata, exception) -> {

                    // skip when send is success
                    if (exception == null) {
                        log.debug("send message successfully (msgId={})", request.getHeader().getMsgId());
                        return;
                    }
                    log.debug("send fail in kafka producer (msgId={})", request.getHeader().getMsgId());

                    // pop transaction which was failed
                    transactionManager
                            .pop(request.getHeader().getMsgId())
                            .ifPresent(observer -> {
                                observer.onNext(
                                        sendMessageResponse.newBuilder()
                                                .setHeader(request.getHeader())
                                                .setResult(FAILURE)
                                                .setHttpCode(-1)
                                                .setJsonBody(exception.getMessage())
                                                .build()
                                );
                                observer.onCompleted();
                            });
                });
    }
}
