package com.sktelecom.blockchain.msgexpress.client;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.config.HttpServerConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.HttpServer;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExRestAPI;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method.POST;
import static com.sktelecom.blockchain.byzantium.utilities.TimeUtils.getUnixTimeStamp;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto.MsgType.BUS_RESPONSE;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Slf4j
public class MsgExSDKTest {

    /** json parser */
    private Gson gson = new Gson();

    @Test @Ignore
    public void call() {

        // message express client
        MsgExSDK msgExSDK = new MsgExSDK(

                // server config
                HttpServerConfigDto
                        .builder()
                        .httpHost("0.0.0.0")
                        .httpPort(7912)
                        .maxThreads(8)
                        .minThreads(4)
                        .timeout(30)
                        .urlPath("/")
                        .docRoot("/index.html")
                        .build(),

                // client config
                HttpClientConfigDto
                        .builder()
                        .baseUrl("/")
                        .host("0.0.0.0")
                        .port(7777)
                        .connectTimeout(5)
                        .maxIdleConnections(8)
                        .readTimeout(3)
                        .writeTimeout(3)
                        .keepAliveDuration(5)
                        .build(),

                // count of partition
                6,

                // rest handlers
                new HttpServer.Handler (POST, "verify", (request, response) -> {

                    String body = request.body();
                    log.info("[verify] received value is {}", body);


                    MsgExMessageDto message = this.gson.fromJson(body, MsgExMessageDto.class);
                    log.info("[received] value as dto ==> {}", message);

                    return message.toString();
                })
        );

        log.debug("create msgex listener...");

        // send message
        msgExSDK.call(

                "0.0.0..0",

                7777,

                // destination api information
                MsgExRestAPI.builder()
                        .host("0.0.0.0")
                        .port(7912)
                        .api("verify")
                        .method(POST)
                        .jsonBody(gson
                                .toJson(TestDTO.builder()
                                        .id("AAAAA")
                                        .name("Hwang Tae Seung")
                                        .sum(800)
                                        .build()))
                        .build(),

                // transaction callback
                (messageDto, exception) -> {
                    log.info("message = {}", messageDto);
                    // 성공
                    return SC_OK;
                }
        );

        log.debug("rcp has been executed...");

        try {
            System.out.print(System.in.read());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Data @Builder
    static class TestDTO {
        String id;
        String name;
        int sum;
    }
}