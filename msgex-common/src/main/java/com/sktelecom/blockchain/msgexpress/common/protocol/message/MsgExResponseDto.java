package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class MsgExResponseDto {

    /** header */
    private MsgExHeaderDto header;

    /** Rest 처리 결과  */
    private int httpCode;

    /** Rest 처리 Message */
    private String message;
}
