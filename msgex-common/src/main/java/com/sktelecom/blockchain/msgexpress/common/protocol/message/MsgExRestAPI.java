package com.sktelecom.blockchain.msgexpress.common.protocol.message;


import com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method;
import lombok.Builder;
import lombok.Data;

/**
 * Rest API
 */
@Data @Builder
public class MsgExRestAPI {
    // rest server
    private String host;
    // rest port
    private int port;
    // rest method
    private Method method;
    // api uri
    private String api;
    // JSON Payload
    private String jsonBody;

    /**
     * generate URI String
     * @return
     */
    public String getUri() {

        StringBuffer result = new StringBuffer()
                .append("http://")
                .append(this.host)
                .append(":")
                .append(this.port)
                .append("/")
                .append(this.api);

        return result.toString();
    }
}