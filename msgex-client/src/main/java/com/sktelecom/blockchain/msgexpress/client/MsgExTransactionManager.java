package com.sktelecom.blockchain.msgexpress.client;

import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@Slf4j
class MsgExTransactionManager {

    /** count of partition */
    private @Getter int countOfPartition;

    /** transaction map */
    private List<ConcurrentHashMap<String, Callback>> transactions;

    // default transaction callback
    private @Getter Callback defaultTransactionCallback = (message, exception) -> {
        log.warn("transaction not found (msg_id={})", message.getHeader().getMsgId());
        return SC_INTERNAL_SERVER_ERROR;
    };

    /**
     * construction
     * @param countOfPartition
     */
    MsgExTransactionManager(int countOfPartition) {

        // partition 수
        this.countOfPartition = countOfPartition;

        // create transaction map
        this.transactions = IntStream.range(0, countOfPartition)
                .mapToObj(index -> new ConcurrentHashMap<String, Callback>())
                .collect(Collectors.toList());
    }

    /**
     * push transaction
     * @param messageId
     * @param transactionCallback
     */
    Callback push(String messageId, Callback transactionCallback) {
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
    Optional<Callback> pop(String messageId) {
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

    /**
     * Callback interface
     */
    interface Callback extends BiFunction<MsgExMessageDto, Exception, Integer> {
    }
}
