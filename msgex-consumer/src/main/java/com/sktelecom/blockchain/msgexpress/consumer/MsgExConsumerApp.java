package com.sktelecom.blockchain.msgexpress.consumer;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.application.AbstractApplication;
import com.sktelecom.blockchain.byzantium.config.HttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.RawHttpClient;
import com.sktelecom.blockchain.byzantium.queue.kafka.KConsumer;
import com.sktelecom.blockchain.byzantium.queue.kafka.KProducer;
import com.sktelecom.blockchain.byzantium.utilities.TimeUtils;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExHeaderDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExRestAPI;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;

import static com.sktelecom.blockchain.byzantium.application.AppPropConfiguration.loadConfig;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.*;
import static java.lang.Integer.parseInt;

@Slf4j
public class MsgExConsumerApp extends AbstractApplication<Properties> {

    /** configuration file name */
    private static final String APP_CONFIG_FILE = "/application.properties";

    /** default consumer thread count */
    private final static String DEFAULT_CONSUMER_THREAD_COUNT = "5";

    /** kafka consumer */
    private KConsumer<String, String> consumer;

    /** kafka producer */
    private KProducer<String, String> producer;

    /** http client */
    private RawHttpClient restApiClient;

    /** json parser */
    private static Gson gson = new Gson();

    /** send back topics */
    private String send_back_topics;

    /**
     * constructor
     */
    private MsgExConsumerApp() throws IOException {
        // create supplier to create config object
        super(loadConfig(APP_CONFIG_FILE));
    }

    /**
     * application run
     * @param properties
     */
    @Override
    protected void run(Properties properties) {

        // set serializer to encode key&value
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());

        // create kafka configuration
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());

        // set topics
        this.send_back_topics = properties.getProperty("msgex.consumer.send_back_topics", DEFAULT_TOPIC + "-receive");

        // create http client pools
        this.restApiClient = new RawHttpClient(getConfig(properties));

        // create producer
        this.producer = new KProducer<>(() -> new KafkaProducer<>(properties));

        // create consumer object
        this.consumer = new KConsumer<String, String>(

                // supplier
                () -> {
                    // create kafka consumer object
                    return new KafkaConsumer<>(properties);
                },

                // recipient callback
                record -> {

                    log.debug("receive record from kafka : topic=>{}, key=>{}, value=>{}", record.topic(), record.key(), record.value());

                    // parse json
                    MsgExMessageDto message = gson.fromJson((String) record.value(), MsgExMessageDto.class);
                    MsgExRestAPI restAPI = message.getDestinationAPI();

                    // check error
                    MsgExResponseDto.MsgExResponseDtoBuilder builder = MsgExResponseDto
                            .builder()
                            .header(MsgExHeaderDto
                                    .builder()
                                    .msgId(message.getHeader().getMsgId())
                                    .msgType(MsgExHeaderDto.MsgType.RESPONSE)
                                    .timestamp(TimeUtils.getUnixTimeStamp())
                                    .senderIP(message.getDestinationAPI().getHost())
                                    .senderTag(message.getDestinationAPI().getUri())
                                    .build());

                    try {
                        // call
                        Response response = this.restApiClient.call(restAPI.getMethod(), restAPI.getUri(), restAPI.getJsonBody());
                        String jsonBody = response.body() != null ? response.body().string() : "{ message : 'none' }";

                        log.debug("response json body from REST API = {}", jsonBody);

                        // check error
                        builder.result(MsgExResponseDto.MsgExResult.SUCCESS)
                                .httpCode(response.code())
                                .jsonBody(jsonBody);

                    } catch (Exception e) {
                        log.error("restAPI error... {}", message.getDestinationAPI(), e);
                        builder.result(MsgExResponseDto.MsgExResult.FAILURE)
                                .httpCode(-1)
                                .jsonBody("{ exception : " + e.getMessage() + "}");
                    }

                    // send back to kafka
                    this.producer.send(this.send_back_topics,

                            // key
                            message.getHeader().getMsgId(),

                            // value
                            gson.toJson(builder.build()),

                            // kafka callback
                            (metadata, exception) -> {

                                // skip when send is success
                                if (exception == null) {
                                    log.debug("send back message successfully (msgId={})", message.getHeader().getMsgId());
                                    return;
                                }
                                log.debug("send back fail in kafka producer (msgId={})", message.getHeader().getMsgId());
                            });
                },

                // commit callback
                (offsets, exception) -> {
                    // print commit offset information
                    offsets.forEach((topicPartition, offsetAndMetadata) -> {
                        log.debug("received massage : topic={}, partition={}, meta={}, offset={}",
                                topicPartition.topic(), topicPartition.partition(), offsetAndMetadata.metadata(), offsetAndMetadata.offset());
                    });
                },

                // count of consumer thread (or consumer)
                parseInt(properties.getProperty("msgex.consumer.consumer_thread_count", DEFAULT_CONSUMER_THREAD_COUNT)),

                // topics to subscribe
                properties.getProperty("msgex.consumer.receive_topics", DEFAULT_TOPIC + "-send"))

                // run
                .run();

        log.info("MsgExConsumer has been started...");
    }

    /**
     * application shutdown callback
     * @param properties
     * @throws Exception
     */
    @Override
    protected void shutdown(Properties properties) throws Exception {
        // consumer 종료
        this.consumer.shutdown();
        log.info("MsgExConsumer has been terminated gracefully.");

        // log 기록 종료 대기
        Thread.sleep(3000);
    }

    /**
     * Config
     * @param properties
     * @return
     */
    private HttpClientConfigDto getConfig(Properties properties) {

        return HttpClientConfigDto
                .builder()
                .readTimeout(parseInt(properties.getProperty("msgex.consumer.httpclient.readTimeout", DEFAULT_HTTP_READ_TIMEOUT)))
                .connectTimeout(parseInt(properties.getProperty("msgex.consumer.httpclient.connectTimeout", DEFAULT_HTTP_CONNECT_TIMEOUT)))
                .writeTimeout(parseInt(properties.getProperty("msgex.consumer.httpclient.writeTimeout", DEFAULT_HTTP_WRITE_TIMEOUT)))
                .maxIdleConnections(parseInt(properties.getProperty("msgex.consumer.httpclient.maxIdleConnections", DEFAULT_HTTP_MAX_IDLE_CONNECTIONS)))
                .keepAliveDuration(parseInt(properties.getProperty("msgex.consumer.httpclient.keepAliveDuration", DEFAULT_HTTP_KEEP_ALIVE_DURATION)))
                .build();
    }

    /**
     * application main
     * @param argv
     * @throws Exception
     */
    public static void main(String argv[]) throws Exception {
        new MsgExConsumerApp().execute();
    }
}
