package com.sktelecom.blockchain.msgexpress.consumer;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.application.AbstractApplication;
import com.sktelecom.blockchain.byzantium.config.RawHttpClientConfigDto;
import com.sktelecom.blockchain.byzantium.network.http.RawHttpClient;
import com.sktelecom.blockchain.byzantium.queue.kafka.KConsumer;
import com.sktelecom.blockchain.msgexpress.client.MsgExSDK;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sktelecom.blockchain.byzantium.application.AppPropConfiguration.loadConfig;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.*;
import static java.lang.Integer.parseInt;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Slf4j
public class MsgExConsumerApp extends AbstractApplication<Properties> {

    /** configuration file name */
    private static final String APP_CONFIG_FILE = "/application.properties";

    /** default consumer thread count */
    private final static String DEFAULT_CONSUMER_THREAD_COUNT = "5";

    /** kafka consumer */
    private KConsumer<String, String> consumer;

    /** http client */
    private RawHttpClient restApiClient;

    /** thread pool */
    private ExecutorService executorService;

    /** json parser */
    private Gson gson;

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

        // create kafka configuration
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());

        // create http client pools
        this.restApiClient = new RawHttpClient(getConfig(properties));

        // create execute service
        this.executorService = Executors.newFixedThreadPool(8);

        // create gson
        this.gson = new Gson();

        // create consumer object
        this.consumer = new KConsumer<>(

                // supplier
                () -> {
                    // create kafka consumer object
                    return new KafkaConsumer<>(properties);
                },

                // recipient callback
                record -> {

                    log.debug("record : topic=>{}, key=>{}, value=>{}", record.topic(), record.key(), record.value());

                    // parse json
                    MsgExMessageDto message = this.gson.fromJson((String) record.value(), MsgExMessageDto.class);

                    log.debug("call API URI={}, JSON-{}", message.getDestinationAPI().getUri(), message.getDestinationAPI().getJsonBody());

                    try {
                        Response response = this.restApiClient.post(message.getDestinationAPI().getUri(), message.getDestinationAPI().getJsonBody());

                        String bodyString = response.body() != null ? response.body().string() : "{}";

                        // transfer message
                        MsgExSDK.reply(message, bodyString,

                                // callback
                                messageDto -> {

                                    String uri = message.getDestinationAPI().getUri();
                                    String payload = message.getDestinationAPI().getJsonBody();

                                    log.debug("-- listen uri = {}", uri);
                                    log.debug("-- json = {}", payload);

                                    Response res = this.restApiClient.post(uri, payload);

                                    log.debug("-- result = {}", bodyString);

                                    return res.code() == SC_OK ? retrofit2.Response.success(gson.fromJson(res.body().string(), MsgExResponseDto.class)) :
                                            retrofit2.Response.error(res.code(), res.body());
                                },

                                // execute service
                                this.executorService
                        );

                    } catch (Exception e) {
                        log.error("restAPI error... {}", message.getDestinationAPI(), e);
                        e.printStackTrace();
                    }

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
                parseInt(properties.getProperty("msgex.consumer.consumerThreadCount", DEFAULT_CONSUMER_THREAD_COUNT)),

                // topics to subscribe
                properties.getProperty("msgex.consumer.topic", DEFAULT_TOPIC)
        );

        // run
        this.consumer.run();
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
    private RawHttpClientConfigDto getConfig(Properties properties) {
        return RawHttpClientConfigDto
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
