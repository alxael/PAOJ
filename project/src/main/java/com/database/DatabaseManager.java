package com.database;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class DatabaseManager implements AutoCloseable {
    private static DatabaseManager instance;

    private final Connection connection;

    private final CurrencyEntity currencyEntity;
    private final ExchangeEntity exchangeEntity;

    private void initializeDatabase(String filePath) {
        initializeDatabase(filePath, ";");
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
                } catch (SQLException ignoredException) {
                    // log error here
                }
            }

            scanner.close();
        } catch (Exception ignoredException) {
            // log unexpected error here
        }
    }

    public DatabaseManager(Connection connection, String initializationFilePath) {
        this.connection = connection;

        this.currencyEntity = new CurrencyEntity(connection);
        this.exchangeEntity = new ExchangeEntity(connection, currencyEntity);

        initializeDatabase(initializationFilePath);

        try {
            currencyEntity.loadData();
            exchangeEntity.loadData();
        } catch (SQLException ignoredException) {
            // log error here
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
        // log connection closing
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    public CurrencyEntity getCurrencyEntity() {
        return currencyEntity;
    }

    public ExchangeEntity getExchangeEntity() {
        return exchangeEntity;
    }
}
