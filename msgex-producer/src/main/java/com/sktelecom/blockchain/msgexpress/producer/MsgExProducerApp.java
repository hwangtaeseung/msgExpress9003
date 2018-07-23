package com.sktelecom.blockchain.msgexpress.producer;

import com.sktelecom.blockchain.byzantium.application.AbstractApplication;
import com.sktelecom.blockchain.byzantium.config.HttpServerConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.HttpServer;
import com.sktelecom.blockchain.byzantium.queue.kafka.KProducer;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.sktelecom.blockchain.byzantium.application.AppPropConfiguration.loadConfig;
import static com.sktelecom.blockchain.byzantium.network.http.HttpServer.Method.POST;
import static com.sktelecom.blockchain.byzantium.utilities.TimeUtils.getUnixTimeStamp;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.*;
import static java.lang.Integer.parseInt;

/**
 * Producer application
 */
@Slf4j
public class MsgExProducerApp extends AbstractApplication<Properties> {

    /** configuration file name */
    private static final String APP_CONFIG_FILE = "/application.properties";

    /** default host */
    private static final String DEFAULT_HOST = "0.0.0.0";

    /** default port */
    private static final String DEFAULT_LISTEN_PORT = "7408";

    /** min thread */
    private static final String DEFAULT_MIN_THREAD = "4";

    /** max thread */
    private static final String DEFAULT_MAX_THREAD = "8";

    /** kafka send timeout */
    private static final String DEFAULT_HTTP_TIMEOUT = "2";

    /** Kafka Producer */
    private KProducer<String, String> producer;

    /** Http Server */
    private HttpServer httpServer;

    /**
     * constructor
     */
    private MsgExProducerApp() throws IOException {
        // create supplier to create config object
        super(loadConfig(APP_CONFIG_FILE));
    }

    /**
     * execution logic implementation
     * @param properties
     */
    @Override
    protected void run(Properties properties) {

        // set serializer to encode key&value
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());

        // create producer
        this.producer = new KProducer<>(()-> new KafkaProducer<>(properties));

        // get config
        HttpServerConfigDto config = getConfig(properties);

        // http server
        this.httpServer = new HttpServer()

                // handler to transfer message to MS
                .addHandler(POST, PRODUCER_URI + MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK, (message, response) -> {

                    log.debug("** received message is {}", message.body());

                    // header
                    MsgExHeaderDto header = MsgExHeaderDto
                            .builder()
                            .msgId(message.params(MESSAGE_RECEIVE_URI_PARAM_FOR_SPARK))
                            .msgType(MsgExHeaderDto.MsgType.BUS_RESPONSE)
                            .timestamp(getUnixTimeStamp())
                            .senderIP(config.getHttpHost())
                            .senderTag("KAFKA-Producer")
                            .build();

                    // response
                    MsgExResponseDto responseDto = MsgExResponseDto
                            .builder()
                            .header(header)
                            .build();

                    // send message to kafka
                    try {
                        producer.send(DEFAULT_TOPIC, header.getMsgId(), message.body())
                                .get(parseInt(properties.getProperty("msgex.producer.sendtimeout", DEFAULT_HTTP_TIMEOUT)), TimeUnit.SECONDS);

                        // 성공
                        responseDto.setHttpCode(response.status());
                        responseDto.setMessage(PRODUCER_URI);

                    } catch (Exception e) {

                        log.error("kafka producer error", e);

                        // failure
                        responseDto.setHttpCode(500);
                        responseDto.setMessage(e.getMessage());
                        response.status(responseDto.getHttpCode());
                    }

                    return responseDto;
                })

                // execute server
                .run(config);

        log.info("MsgExProducer has been started...");
    }

    /**
     * shutdown callback implementation
     * @param properties
     */
    @Override
    protected void shutdown(Properties properties) throws InterruptedException {

        // shutdown kafka client
        this.producer.close();

        // shutdown http server
        this.httpServer.shutdown();

        log.info("MsgExProducer has been terminated gracefully.");

        // log 기록 종료 대기
        Thread.sleep(3000);
    }

    /**
     * create config object
     * @param properties
     * @return
     */
    private HttpServerConfigDto getConfig(Properties properties) {
        // config
        return HttpServerConfigDto
                .builder()
                .httpHost(properties.getProperty("msgex.producer.host", DEFAULT_HOST))
                .httpPort(parseInt(properties.getProperty("msgex.producer.port", DEFAULT_LISTEN_PORT)))
                .maxThreads(parseInt(properties.getProperty("msgex.producer.maxThread", DEFAULT_MAX_THREAD)))
                .minThreads(parseInt(properties.getProperty("msgex.producer.minThread", DEFAULT_MIN_THREAD)))
                .timeout(parseInt(properties.getProperty("msgex.producer.timeout", DEFAULT_LISTEN_PORT)))
                .urlPath("/")
                .build();
    }

    /**
     * application main
     * @param argv
     * @throws Exception
     */
    public static void main(String argv[]) throws Exception {
        // application 실행
        new MsgExProducerApp().execute();
    }
}
