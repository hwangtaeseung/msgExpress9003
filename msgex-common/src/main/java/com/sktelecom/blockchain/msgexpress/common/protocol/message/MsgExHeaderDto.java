package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data @Builder
public class MsgExHeaderDto {

    /** msg id which is unique globally */
    private String msgId;

    /** basic msg type */
    private MsgType msgType;

    /** unix timestamp */
    private long timestamp;

    /** sender IP */
    private String senderIP;

    /** sender Tag */
    private String senderTag;

    /**
     * basic message type
     */
    public enum MsgType {

        REQUEST(0),
        RESPONSE(1);

        @Getter int value;
        MsgType(int value) {
            this.value = value;
        }

        /**
         * to MsgType
         * @param value
         * @return
         */
        public static MsgType toMsgType(int value) {
            return values()[value];
        }
    }
}