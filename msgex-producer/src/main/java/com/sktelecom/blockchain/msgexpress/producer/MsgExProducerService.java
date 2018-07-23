package com.sktelecom.blockchain.msgexpress.producer;


import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.queue.kafka.KProducer;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.ProduceServerGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.ResponseSystem.KAFKA;
import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.Result.FAILURE;
import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.Result.SUCCESS;

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


    /**
     * constructor
     * @param producer
     */
    MsgExProducerService(KProducer<String, String> producer, Properties properties) {
        this.producer = producer;
        this.topics = properties.getProperty("msgex.producer.topics", null);
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
                request.getPayload());

        // send msg to kafka
        this.producer.send(this.topics != null ? this.topics : request.getHeader().getTopicName(),

                request.getHeader().getMsgId(),

//                gson.toJson(MsgExMessageConverter.convert(request)),
                "",

                (metadata, exception) -> {

                    // 응답 Message
                    sendMessageResponse response = sendMessageResponse.newBuilder()
                            .setHeader(request.getHeader())
                            .setResult(exception != null ? SUCCESS : FAILURE)
                            .setCalledApi(request.getDestinationAPI())
                            .setResponseSystem(KAFKA)
                            .setHttpCode(-1)
                            .setPayload(exception != null ? exception.getMessage() : "none")
                            .build();

                    // grpc 회신
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
        });
    }
}
