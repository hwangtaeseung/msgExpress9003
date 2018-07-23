package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class MsgExMessageDto {

    /** message header */
    private MsgExHeaderDto header;

    /** 호출한 API의 응답을 받기 위한 API */
    private int receivePort;

    /** 응답을 받을지 여부 */
    private boolean needToReply;

    /** 호출할 API */
    private MsgExRestAPI destinationAPI;

    /** rest API 호출 재시도 회수 */
    private int retryCount;

    /** rest API 호출 timeout */
    private int timeout;
}
