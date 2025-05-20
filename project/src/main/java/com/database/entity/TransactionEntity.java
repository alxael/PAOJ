package com.database.entity;

import com.bank.Transaction;
import com.database.Entity;
import com.database.Query;
import com.exception.InvalidBusinessLogicException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class TransactionEntity extends Entity<Long, Transaction> {
    private final AccountEntity accountEntity;
    private final ExchangeEntity exchangeEntity;

    @Override
    protected SimpleEntry<Long, Transaction> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var inboundId = resultSet.getLong("inbound");
        var outboundId = resultSet.getLong("outbound");
        var amount = resultSet.getDouble("amount");
        var date = resultSet.getDate("date");

        var inbound = accountEntity.getRecordById(inboundId, true);
        var outbound = accountEntity.getRecordById(outboundId, true);
        var transaction = new Transaction(inbound.getValue(), outbound.getValue(), amount, date.toLocalDate().atStartOfDay());
        return new SimpleEntry<>(id, transaction);
    }

    public TransactionEntity(Connection connection, AccountEntity accountEntity, ExchangeEntity exchangeEntity) {
        super("transactions", connection);
        this.accountEntity = accountEntity;
        this.exchangeEntity = exchangeEntity;
    }

    public SimpleEntry<Long, Transaction> createTransaction(Long userId, String inboundIBAN, String outboundIBAN, Double amount) throws SQLException {
        var inbound = accountEntity.getAccountFromIBAN(inboundIBAN);
        var outbound = accountEntity.getAccountFromIBAN(outboundIBAN);

        var userAccounts = accountEntity.getUserAccounts(userId);
        if (!userAccounts.containsKey(inbound.getKey())) {
            throw new InvalidBusinessLogicException("You may only transfer money from your own account!");
        }

        var inboundCurrency = inbound.getValue().getCurrency();
        var outboundCurrency = outbound.getValue().getCurrency();
        var exchange = exchangeEntity.getExchangeFromCurrencyCodes(outboundCurrency.getCode(), inboundCurrency.getCode());

        var rate = exchange.getValue().getRate();
        var newOutboundAmount = outbound.getValue().getAmount() - amount;
        if (newOutboundAmount < 0) {
            throw new InvalidBusinessLogicException("Insufficient funds for transaction!");
        }
        var newInboundAmount = inbound.getValue().getAmount() + amount * rate;

        accountEntity.updateAccountAmount(inbound.getKey(), newInboundAmount);
        accountEntity.updateAccountAmount(outbound.getKey(), newOutboundAmount);

        var now = LocalDateTime.now();

        var query = new Query(connection, "INSERT INTO :table VALUES (DEFAULT, :inbound, :outbound, :amount, :date);");
        query.setParameter("table", table, false)
                .setParameter("inbound", inbound.getKey())
                .setParameter("outbound", outbound.getKey())
                .setParameter("amount", amount)
                .setParameter("date", now.toString());
        query.execute();

        var entry = getRecordByProperty("date", now.toString());
        data.put(entry.getKey(), entry.getValue());
        return entry;
    }

    public SimpleEntry<Map<Long, Transaction>, Map<Long, Transaction>> getAccountTransactions(Long accountId) throws SQLException {
        var inboundTransactions = getRecordsByProperty("inbound", accountId.toString());
        var outboundTransactions = getRecordsByProperty("outbound", accountId.toString());
        return new SimpleEntry<>(inboundTransactions, outboundTransactions);
    }
}
