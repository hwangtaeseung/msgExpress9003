package com.sktelecom.blockchain.msgexpress.common.protocol.message;

import com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

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
    // rest parameters
    private Map<String, Object> params;
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

        if (this.params != null && !this.params.isEmpty()) {
            result.append("?");
            this.params.forEach((key, value) -> result.append(key).append("=").append(value));
        }

        return result.toString();
    }
}