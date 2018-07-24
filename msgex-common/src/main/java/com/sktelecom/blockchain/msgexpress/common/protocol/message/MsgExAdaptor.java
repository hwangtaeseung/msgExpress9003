package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import com.sktelecom.blockchain.byzantium.network.http.HttpServer;
import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.*;

public class MsgExAdaptor {

    /**
     * GRPC to DTO
     * @param request
     * @return
     */
    public static MsgExMessageDto toMsgExMessage(sendMessageRequest request) {
        return MsgExMessageDto
                .builder()
                .header(toMsgExHeader(request.getHeader()))
                .needToReply(request.getNeedToReply())
                .destinationAPI(toMsgExRestAPI(request.getDestinationAPI()))
                .retryCount(request.getRetryCount())
                .timeout(request.getTimeout())
                .build();
    }

    /**
     * DTO -> GRPC
     * @param responseDto
     * @return
     */
    public static sendMessageResponse toSendMessageResponse(MsgExResponseDto responseDto) {
        return sendMessageResponse.newBuilder()
                .setHeader(toMsgHeader(responseDto.getHeader()))
                .setResult(Result.values()[responseDto.getResult().getValue()])
                .setHttpCode(responseDto.getHttpCode())
                .setJsonBody(responseDto.getJsonBody())
                .build();
    }

    /**
     * DTO -> GRPC
     * @param restAPI
     * @return
     */
    public static RestAPI toRestAPI(MsgExRestAPI restAPI) {
        return RestAPI.newBuilder()
                .setHost(restAPI.getHost())
                .setPort(restAPI.getPort())
                .setMethod(Method.values()[restAPI.getMethod().getValue()])
                .setRestApi(restAPI.getApi())
                .setJsonBody(restAPI.getJsonBody())
                .build();
    }

    /**
     * DTO -> GRPC
     * @param restAPI
     * @return
     */
    public static MsgExRestAPI toMsgExRestAPI(RestAPI restAPI) {
        return MsgExRestAPI.builder()
                .host(restAPI.getHost())
                .port(restAPI.getPort())
                .method(HttpServer.Method.values()[restAPI.getMethod().getNumber()])
                .api(restAPI.getRestApi())
                .jsonBody(restAPI.getJsonBody())
                .build();
    }

    /**
     * Header (GRPC -> DTO)
     * @param header
     * @return
     */
    public static MsgExHeaderDto toMsgExHeader(MessageHeader header) {
        return MsgExHeaderDto
                .builder()
                .msgId(header.getMsgId())
                .msgType(MsgExHeaderDto.MsgType.toMsgType(header.getMsgType().getNumber()))
                .timestamp(header.getTimestamp())
                .build();
    }

    /**
     * Header (DTO -> GRPC)
     * @param headerDto
     * @return
     */
    public static MessageHeader toMsgHeader(MsgExHeaderDto headerDto) {
        return MessageHeader.newBuilder()
                .setMsgId(headerDto.getMsgId())
                .setMsgTypeValue(headerDto.getMsgType().value)
                .setTimestamp(headerDto.getTimestamp())
                .build();
    }
}
