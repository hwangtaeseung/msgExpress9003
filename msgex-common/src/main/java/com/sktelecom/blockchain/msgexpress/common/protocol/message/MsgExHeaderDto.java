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

        API_REQUEST((byte) 0),
        API_RESPONSE((byte) 1),
        BUS_REQUEST((byte) 2),
        BUS_RESPONSE((byte) 3);

        @Getter byte value;
        MsgType(byte value) {
            this.value = value;
        }
    }
}