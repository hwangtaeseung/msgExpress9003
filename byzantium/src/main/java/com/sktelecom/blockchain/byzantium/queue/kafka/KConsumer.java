package com.sktelecom.blockchain.byzantium.queue.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Kafka Consumer
 * @param <K>
 * @param <V>
 */
@Slf4j
public class KConsumer<K, V> {

    final private static int WAIT_TIME_FOR_TERMINATING = 2;
    final private static int POLLING_TIME_AS_MSEC = 100;

    /** kafka consumer callable */
    private List<ConsumerRunnable> consumers;

    /** thread pool **/
    private ExecutorService executorService;

    /**
     * 생성자
     * @param supplier
     */
    public KConsumer(Supplier<Consumer<K, V>> supplier,
                     java.util.function.Consumer<ConsumerRecord> callback,
                     OffsetCommitCallback commitCallback,
                     int consumerThreadCount, String ...topics) {

        // thread pool
        this.executorService = Executors.newFixedThreadPool(consumerThreadCount);

        // consumer callable
        this.consumers = IntStream
                .range(0, consumerThreadCount)
                .mapToObj(index -> new ConsumerRunnable(new ConsumerLogic<>(supplier.get())) {
                    @Override
                    public void run() {
                        // 구독할 topic 지정 & 실행
                        this.consumerLogic
                                .setTopics(topics)
                                .run(callback, commitCallback);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 실행
     * @return
     */
    public KConsumer<K, V> run() {
        // execute work threads
        this.consumers.forEach(runnable -> this.executorService.submit(runnable));
        return this;
    }

    /**
     * 종료
     */
    public void shutdown() throws InterruptedException {

        // set running flag to false
        this.consumers.forEach(runnable -> runnable.consumerLogic.stop());

        // Threads 종료할 때까지 대기
        this.waitForTerminating();

        // 종료
        this.executorService.shutdown();
    }

    /**
     * 각 consumer threads 종료할 때 까지 대기
     * @throws InterruptedException
     */
    public void waitForTerminating() throws InterruptedException {
        // 각 Thread 가 종료될 때 까지 대기.
        while (this.executorService.awaitTermination(WAIT_TIME_FOR_TERMINATING, TimeUnit.SECONDS)) {
            log.debug("wait for terminating threads...");
        }
    }

    /**
     * Consumer 실행 Logic
     */
    @Slf4j
    static class ConsumerLogic<K, V> {

        /** kafka consumer object */
        private Consumer<K,V> consumer;

        /** running flag */
        private AtomicBoolean running = new AtomicBoolean(false);

        /**
         * constructor
         * @param consumer
         */
        public ConsumerLogic(Consumer<K, V> consumer) {
            this.consumer = consumer;
        }

        /**
         * consumer 실행
         * @param callback
         * @param commitCallback
         */
        void run(java.util.function.Consumer<ConsumerRecord> callback, OffsetCommitCallback commitCallback) {

            // 실행중 설정
            this.running.set(true);

            // fetch record from kafka topic
            while (this.running.get()) {

                // polling
                ConsumerRecords<K, V> records = this.consumer.poll(POLLING_TIME_AS_MSEC);

                if (records.isEmpty()) continue;

                // iterate records
                records.forEach(callback::accept);

                // commit
                this.consumer.commitAsync(commitCallback);
            }
            log.debug("consumer has been terminated...");

            // consumers 종료
            this.consumer.close();
        }

        /**
         * 구독할 topics 설정
         * @param topics
         * @return
         */
        ConsumerLogic<K, V> setTopics(String... topics) {
            this.consumer.subscribe(Arrays.asList(topics));
            return this;
        }

        /**
         * consumer 정지
         */
        void stop() {
            this.running.set(false);
        }
    }

    /**
     * Runnable for KConsumer
     */
    abstract class ConsumerRunnable implements Runnable {
        ConsumerLogic<K, V> consumerLogic;
        ConsumerRunnable(ConsumerLogic<K, V> consumerLogic) {
            this.consumerLogic = consumerLogic;
        }
    }
}
