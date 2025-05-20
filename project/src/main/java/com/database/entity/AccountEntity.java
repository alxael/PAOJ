package com.database.entity;

import com.bank.Account;
import com.database.Entity;
import com.database.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class AccountEntity extends Entity<Long, Account> {
    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);
    private final TransactionEntity transactionEntity;
    private final CurrencyEntity currencyEntity;
    private final UserEntity userEntity;

    @Override
    protected SimpleEntry<Long, Account> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var currencyId = resultSet.getLong("currency");
        var userId = resultSet.getLong("associateduser");
        var IBAN = resultSet.getString("iban");
        var amount = resultSet.getDouble("amount");
        var firstName = resultSet.getString("firstname");
        var lastName = resultSet.getString("lastname");

        var currency = currencyEntity.getRecordById(currencyId, true);
        var user = userEntity.getRecordById(userId, true);
        var account = new Account(currency.getValue(), user.getValue(), IBAN, amount, firstName, lastName);
        return new SimpleEntry<>(id, account);
    }

    public AccountEntity(
            Connection connection,
            CurrencyEntity currencyEntity,
            UserEntity userEntity,
            TransactionEntity transactionEntity
    ) {
        super("accounts", connection);
        this.currencyEntity = currencyEntity;
        this.userEntity = userEntity;
        this.transactionEntity = transactionEntity;
    }

    public SimpleEntry<Long, Account> getAccountFromIBAN(String IBAN) throws SQLException {
        return getRecordByProperty("iban", IBAN);
    }

    public Map<Long, Account> getUserAccounts(Long userId) throws SQLException {
        return getRecordsByProperty("associateduser", userId.toString());
    }

    public SimpleEntry<Long, Account> createAccount(String currencyCode, Long userId, String firstName, String lastName) throws SQLException {
        var currency = currencyEntity.getCurrencyFromCode(currencyCode);
        var user = userEntity.getRecordById(userId);
        var account = new Account(currency.getValue(), user.getValue(), firstName, lastName);

        log.info(account.toString());

        var query = new Query(connection, "INSERT INTO :table VALUES (DEFAULT, :currency, :user, :iban, :amount, :firstName, :lastName);");
        query.setParameter("table", table, false)
                .setParameter("currency", currency.getKey())
                .setParameter("user", user.getKey())
                .setParameter("iban", account.getIBAN())
                .setParameter("amount", account.getAmount())
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName);
        query.execute();

        var entry = getAccountFromIBAN(account.getIBAN());
        data.put(entry.getKey(), entry.getValue());
        return entry;
    }

    public void updateAccountAmount(Long accountId, Double newAmount) throws SQLException {
        var query = new Query(connection, "UPDATE :table SET amount=:amount WHERE id=:id;");
        query.setParameter("table", table, false)
                .setParameter("id", accountId)
                .setParameter("amount", newAmount);
        query.execute();
        var newAccount = getRecordById(accountId);
        data.remove(accountId);
        data.put(newAccount.getKey(), newAccount.getValue());
    }

    public void deleteAccount(Long accountId) throws SQLException {
        transactionEntity.deleteRecordsByProperty("inbound", accountId.toString());
        transactionEntity.deleteRecordsByProperty("outbound", accountId.toString());
        deleteRecordById(accountId);
    }
}
