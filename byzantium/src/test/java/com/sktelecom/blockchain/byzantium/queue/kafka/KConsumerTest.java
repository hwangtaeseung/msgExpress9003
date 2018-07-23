package com.sktelecom.blockchain.byzantium.queue.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
public class KConsumerTest {

    private final static String DEFAULT_TOPIC = "unitTest";
    private final static int DEFAULT_WORK_THREAD_NUM = 8;

    private final static String keyPrefix = "KEY_";
    private final static String valuePrefix = "VALUES_안녕하세여!!!_";

    @Test @Ignore
    public void run() throws InterruptedException {

        // test 결과 확인
        ConcurrentHashMap<String, String> checkMap = new ConcurrentHashMap<>();

        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", "localhost:9092");
        producerProperties.put("acks", "all");
        producerProperties.put("key.serializer", StringSerializer.class.getName());
        producerProperties.put("value.serializer", StringSerializer.class.getName());

        // create producer
        KProducer<String, String> producer = new KProducer<>(() -> new KafkaProducer<>(producerProperties));

        // create kafka configuration
        Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", "localhost:9092");
        consumerProperties.put("group.id", "consumer-test");
        consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
        consumerProperties.put("value.deserializer", StringDeserializer.class.getName());

        // create consumer object
        KConsumer<String, String> consumer = new KConsumer<>(

                // supplier
                ()-> {
                    // create kafka consumer object
                    return new KafkaConsumer<>(consumerProperties);
                },

                // recipient callback
                record -> {
                    // 수신결과 저장
                    checkMap.put((String) record.key(), (String) record.value());
                    log.debug("record : topic=>{}, key=>{}, value=>{}",
                            record.topic(), record.key(), record.value());
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
                DEFAULT_WORK_THREAD_NUM,

                // topics to subscribe
                DEFAULT_TOPIC
        );

        // run
        consumer.run();

        // send message 1,000 times
        final int countOfMessage = 1000;
        IntStream
                .range(0, countOfMessage)
                .forEach(index -> {
                    // 전달 할 message 저장
                    checkMap.put(keyPrefix + index, valuePrefix + index);
                    // 송신
                    producer.send(DEFAULT_TOPIC, keyPrefix + index, valuePrefix + index,
                            (metadata, exception) -> {
//                        log.debug("sent message : topic={}, partition={}, offset={}",
//                                        metadata.topic(), metadata.partition(), metadata.offset());
                            });
                    producer.flush();
                });

        // close producer
        producer.close();

        // check process result
        IntStream
                .range(0, countOfMessage)
                .parallel()
                .forEach(index -> {
                    // key&value
                    String key = keyPrefix + index;
                    String value = valuePrefix + index;
                    // test
                    Assert.assertEquals(value, checkMap.get(key));
                });

        Assert.assertEquals(checkMap.size(), countOfMessage);

        // shutdown
        consumer.shutdown();
    }
}