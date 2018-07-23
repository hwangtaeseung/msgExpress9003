package com.sktelecom.blockchain.byzantium.database;

import com.sktelecom.blockchain.byzantium.config.DataSourceConfigDto;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.SerializableTransactionRunner;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

import java.util.function.Function;

@Getter
public class DataSource {

    private Jdbi jdbi;

    /**
     * 생성자
     * @param configDto
     */
    public DataSource(DataSourceConfigDto configDto) {
        // create datasource using db pool
        this.jdbi = Jdbi.create(createDataSource(configDto));

        // set transaction handler
        this.jdbi.setTransactionHandler(new SerializableTransactionRunner());
    }

    /**
     * DataSource 생성
     * @param configDto
     * @return
     */
    private HikariDataSource createDataSource(DataSourceConfigDto configDto) {

        HikariConfig config = new HikariConfig();

        // 기본 접속 설정
        config.setDriverClassName(configDto.getClassName());
        config.setJdbcUrl(configDto.getUrl());
        config.setUsername(configDto.getUserName());
        config.setPassword(configDto.getPassword());

        // DB pool 설정
        if (configDto.getMaximumPoolSize() > 0) {
            config.setMaximumPoolSize(configDto.getMaximumPoolSize());
        }

        if (configDto.getMinimumIdleSize() > 0) {
            config.setMinimumIdle(configDto.getMinimumIdleSize());
        }

        return new HikariDataSource(config);
    }

    /**
     * DML 실행 (transaction)
     * @param transactionCallback
     * @param <RESULT_DTO>
     * @return
     */
    public <RESULT_DTO> RESULT_DTO execSQLAsTransaction(Function<Handle, RESULT_DTO> transactionCallback,
                                                        Function<Exception, RESULT_DTO> failureCallBack) {
        RESULT_DTO resultDto;
        try {
            resultDto = this.jdbi.inTransaction(TransactionIsolationLevel.SERIALIZABLE, transactionCallback::apply);
        } catch (Exception e) {
            resultDto = failureCallBack.apply(e);
        }

        return resultDto;
    }
}
