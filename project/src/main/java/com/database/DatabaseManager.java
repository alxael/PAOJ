package com.database;

import com.bank.User;
import com.database.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class DatabaseManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;

    private final Connection connection;

    private final CurrencyEntity currencyEntity;
    private final CountryEntity countryEntity;
    private final ExchangeEntity exchangeEntity;
    private final UserEntity userEntity;
    private final AccountEntity accountEntity;
    private TransactionEntity transactionEntity;

    private void initializeDatabase(String filePath) {
        initializeDatabase(filePath, "\\");
    }

    private void initializeDatabase(String filePath, String separator) {
        try {
            System.out.println(System.getProperty("user.dir"));
            var scanner = new Scanner(Paths.get(filePath), Charset.defaultCharset());
            scanner.useDelimiter(separator);

            while (scanner.hasNext()) {
                try {
                    var queryString = scanner.next() + separator;
                    var query = new Query(connection, queryString);
                    query.execute();
                } catch (SQLException exception) {
                    log.error("SQL exception: {}", exception.getMessage());
                }
            }

            scanner.close();
        } catch (Exception exception) {
            log.error("Unexpected exception when initializing database: {}", exception.getMessage());
        }
    }

    public DatabaseManager(Connection connection, String initializationFilePath) {
        this.connection = connection;

        this.currencyEntity = new CurrencyEntity(connection);
        this.countryEntity = new CountryEntity(connection);
        this.exchangeEntity = new ExchangeEntity(connection, currencyEntity);
        this.userEntity = new UserEntity(connection, countryEntity);
        this.accountEntity = new AccountEntity(connection, currencyEntity, userEntity, transactionEntity);
        this.transactionEntity = new TransactionEntity(connection, accountEntity, exchangeEntity);

        log.info("Database connection opened.");

        initializeDatabase(initializationFilePath, ";");

        try {
            currencyEntity.loadData();
            countryEntity.loadData();
            exchangeEntity.loadData();
            userEntity.loadData();
            accountEntity.loadData();
            transactionEntity.loadData();
        } catch (Exception exception) {
            log.error("Unexpected exception when initializing manager: {}", exception.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
        log.info("Database connection closed.");
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    public CurrencyEntity getCurrencyEntity() {
        return currencyEntity;
    }

    public CountryEntity getCountryEntity() {
        return countryEntity;
    }

    public ExchangeEntity getExchangeEntity() {
        return exchangeEntity;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public AccountEntity getAccountEntity() {
        return accountEntity;
    }

    public TransactionEntity getTransactionEntity() {
        return transactionEntity;
    }
}
