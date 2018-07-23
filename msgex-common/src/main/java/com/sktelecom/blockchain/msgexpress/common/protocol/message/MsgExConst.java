package com.sktelecom.blockchain.msgexpress.common.protocol.message;

/**
 * Massage Express Common Constants
 */
public class MsgExConst {

    /** message bus topic */
    public final static String DEFAULT_TOPIC = "msgex-bus";

    /** producer URI */
    public final static String PRODUCER_URI = "msgex/transfer/";

    /** producer URI Params */
    public final static String MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK = ":msgId";
    public final static String MESSGAE_RECEIVE_URI_PARAM_FOR_RETROFIT = "{msgId}";

    /** message listener URI */
    public final static String CLIENT_MESSAGE_RECEIVE_URI = "msgListener/";

    /** default consumer thread count */
    public final static String DEFAULT_HTTP_READ_TIMEOUT = "3";
    public final static String DEFAULT_HTTP_CONNECT_TIMEOUT = "3";
    public final static String DEFAULT_HTTP_WRITE_TIMEOUT = "3";
    public final static String DEFAULT_HTTP_MAX_IDLE_CONNECTIONS = "10";
    public final static String DEFAULT_HTTP_KEEP_ALIVE_DURATION = "10";
}
