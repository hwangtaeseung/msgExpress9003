package com.sktelecom.blockchain.msgexpress.producer;

import com.google.gson.Gson;
import com.sktelecom.blockchain.byzantium.application.AbstractApplication;
import com.sktelecom.blockchain.byzantium.network.grpc.GRPCServer;
import com.sktelecom.blockchain.byzantium.queue.kafka.KConsumer;
import com.sktelecom.blockchain.byzantium.queue.kafka.KProducer;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExAdaptor;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;

import static com.sktelecom.blockchain.byzantium.application.AppPropConfiguration.loadConfig;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.DEFAULT_TOPIC;
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

    /** default consumer thread count */
    private static final String DEFAULT_CONSUMER_THREAD_COUNT = "5";

    /** Kafka Producer */
    private KProducer<String, String> producer;

    /** Kafka Consumer */
    private KConsumer<String, String> consumer;

    /** grpc server */
    private GRPCServer<MsgExProducerService> grpcServer;

    /** transaction manager */
    private MsgExTransactionManager transactionManager;

    /** json parser */
    private static Gson gson = new Gson();

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
    protected void run(Properties properties) throws IOException {

        // set serializer to encode key&value
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());

        // set serializer to encode key&value
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());

        // create transaction manager
        this.transactionManager = new MsgExTransactionManager(parseInt(properties.getProperty("msgex.producer.partition_num", "10")));

        // create producer
        this.producer = new KProducer<>(()-> new KafkaProducer<>(properties));

        // create consumer
        this.consumer = new KConsumer<String, String>(

                // supplier
                () -> {
                    // create kafka consumer object
                    return new KafkaConsumer<>(properties);
                },

                // receive callback
                record -> transactionManager
                        .pop((String) record.key())
                        .ifPresent(observer -> {
                            log.debug("receive response message from kafka : msgId={}", record.key());
                            observer.onNext(MsgExAdaptor.toMsgExResponse(gson.fromJson((String) record.value(), MsgExResponseDto.class)));
                            observer.onCompleted();
                        }),

                // commit callback
                (offsets, exception) -> {
                    // print commit offset information
                    offsets.forEach((topicPartition, offsetAndMetadata) ->
                            log.debug("received massage : topic={}, partition={}, meta={}, offset={}",
                            topicPartition.topic(), topicPartition.partition(), offsetAndMetadata.metadata(), offsetAndMetadata.offset()));
                },

                // count of consumer thread (or consumer)
                parseInt(properties.getProperty("msgex.producer.consumer_thread_count", DEFAULT_CONSUMER_THREAD_COUNT)),

                // topics to subscribe
                properties.getProperty("msgex.producer.receive_topics", DEFAULT_TOPIC + "-receive")
        )

        // execute consumer
        .run();

        // grpc server
        this.grpcServer = new GRPCServer<>(new MsgExProducerService(this.producer, this.transactionManager, properties))
                .start(properties.getProperty("msgex.producer.grpc.host", DEFAULT_HOST), parseInt(properties.getProperty("msgex.producer.grpc.port", DEFAULT_LISTEN_PORT)));

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

        log.info("producer has been terminated gracefully.");

        // shutdown consumer
        this.consumer.shutdown();

        log.info("consumer has been terminated gracefully.");

        // shutdown grpc server
        this.grpcServer.stop();

        log.info("grpc server has been terminated gracefully.");

        // log 기록 종료 대기
        Thread.sleep(3000);
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
