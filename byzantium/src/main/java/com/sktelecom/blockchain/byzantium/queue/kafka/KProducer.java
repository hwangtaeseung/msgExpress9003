package com.sktelecom.blockchain.byzantium.queue.kafka;

import lombok.Getter;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Kafka Producer
 * @param <K>
 * @param <V>
 */
@Getter
public class KProducer<K, V> {

    /** kafka producer */
    final private Producer<K, V> producer;

    /**
     * 생성자
     **/
    public KProducer(Supplier<Producer<K,V>> supplier) {
        this.producer = supplier.get();
    }

    /**
     * send message
     * @return
     */
    public Future<RecordMetadata> send(String topic, K key, V message, Callback callback) {
        return this.producer.send(new ProducerRecord<>(topic, key, message), callback);
    }

    /**
     * send message
     * @return
     */
    public Future<RecordMetadata> send(String topic, K key, V message) {
        return this.producer.send(new ProducerRecord<>(topic, key, message));
    }

    /**
     * flush message
     */
    public void flush() {
        this.producer.flush();
    }

    /**
     * 종료
     */
    public void close() {
        this.producer.close();
    }
}
