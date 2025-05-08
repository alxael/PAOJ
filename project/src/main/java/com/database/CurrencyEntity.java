package com.database;

import com.bank.Currency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class CurrencyEntity extends Entity<Long, Currency> {
    @Override
    protected SimpleEntry<Long, Currency> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var name = resultSet.getString("name");
        var code = resultSet.getString("code");

        var currency = new Currency(name, code);
        return new SimpleEntry<>(id, currency);
    }

    public CurrencyEntity(Connection connection) {
        super("currencies", connection);
    }

    public SimpleEntry<Long, Currency> getRecordById(Long id) throws SQLException {
        return getRecordById(id, true);
    }

    public SimpleEntry<Long, Currency> getCurrencyFromCode(String code) throws SQLException {
        var result = getRecordByProperty("code", code);
        var dataResult = data.get(result.getKey());
        return new SimpleEntry<>(result.getKey(), dataResult);
    }

    public List<SimpleEntry<String, String>> getCurrencyDisplayData() throws SQLException {
        var result = new ArrayList<SimpleEntry<String, String>>();
        for (var value : data.values()) {
            result.add(new SimpleEntry<>(value.getCode(), value.getName()));
        }
        return result;
    }
}
