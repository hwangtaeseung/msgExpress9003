package com.sktelecom.blockchain.msgexpress.producer;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse;

@Slf4j
class MsgExTransactionManager {

    /** count of partition */
    private @Getter int countOfPartition;

    /** transaction map */
    private List<ConcurrentHashMap<String, StreamObserver<sendMessageResponse>>> transactions;

    /**
     * construction
     * @param countOfPartition
     */
    MsgExTransactionManager(int countOfPartition) {

        // partition 수
        this.countOfPartition = countOfPartition;

        // create transaction map
        this.transactions = IntStream.range(0, countOfPartition)
                .mapToObj(index -> new ConcurrentHashMap<String, StreamObserver<sendMessageResponse>>())
                .collect(Collectors.toList());
    }

    /**
     * push transaction
     * @param messageId
     * @param transactionCallback
     */
    StreamObserver<sendMessageResponse> push(String messageId, StreamObserver<sendMessageResponse> transactionCallback) {
        // put transaction callback
        return this.transactions
                .get(getPartitionIndex(messageId))
                .put(messageId, transactionCallback);
    }

    /**
     * pop transaction
     * @param messageId
     * @return
     */
    Optional<StreamObserver<sendMessageResponse>> pop(String messageId) {
        // execute exception callback
        return Optional.ofNullable(this.transactions
                .get(getPartitionIndex(messageId))
                .remove(messageId));
    }

    /**
     * hash function to calculate partition index
     * @param msgId
     * @return
     */
    private int getPartitionIndex(String msgId) {
        return Math.abs(msgId.hashCode()) % this.countOfPartition;
    }
}
