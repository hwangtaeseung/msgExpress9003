package com.sktelecom.blockchain.msgexpress.client;

import com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method.POST;

@Slf4j
public class MsgExSDKTest {

    @Test @Ignore
    public void call() throws InterruptedException {

        // create client object
        MsgExSDK sdk = new MsgExSDK("0.0.0.0", 7777, 10);

        // call api
        try {

            sendMessageResponse response = sdk.callApi(
                    // API information
                    "0.0.0.0", 8888, POST, "/api/member", true,

                    // jsonBody
                    TestDTO.builder()
                            .id("XXXX000101010")
                            .name("HwangTaeSeung")
                            .sum(100)
                            .build()
            );

            log.info("result : {}", response.getJsonBody());

        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Data
    @Builder
    static class TestDTO {
        String id;
        String name;
        int sum;
    }
}