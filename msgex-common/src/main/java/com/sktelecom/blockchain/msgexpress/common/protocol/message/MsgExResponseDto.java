package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data @Builder
public class MsgExResponseDto {

    /** header */
    private MsgExHeaderDto header;

    /** MsgEX Result */
    private MsgExResult result;

    /** API Result */
    private int httpCode;

    /** rest Message */
    private String jsonBody;

    /**
     * result
     */
    public enum MsgExResult {

        SUCCESS (0),

        FAILURE (1);

        @Getter int value;

        MsgExResult(int value) {
            this.value = value;
        }
    }
}
