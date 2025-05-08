package com.database;

import com.bank.Exchange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExchangeEntity extends Entity<Long, Exchange> {
    private final CurrencyEntity currencyEntity;

    @Override
    protected SimpleEntry<Long, Exchange> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var sourceId = resultSet.getLong("source");
        var destinationId = resultSet.getLong("destination");
        var rate = resultSet.getDouble("rate");

        var source = currencyEntity.getRecordById(sourceId);
        var destination = currencyEntity.getRecordById(destinationId);

        var exchange = new Exchange(source.getValue(), destination.getValue(), rate);
        return new SimpleEntry<>(id, exchange);
    }

    public ExchangeEntity(Connection connection, CurrencyEntity currencyEntity) {
        super("exchanges", connection);
        this.currencyEntity = currencyEntity;
    }

    public SimpleEntry<Long, Exchange> getRecordById(Long id) throws SQLException {
        return getRecordById(id, true);
    }

    public SimpleEntry<Long, Exchange> getExchangeFromCurrencyCodes(String sourceCode, String destinationCode) throws SQLException {
        var source = currencyEntity.getCurrencyFromCode(sourceCode);
        var destination = currencyEntity.getCurrencyFromCode(destinationCode);

        var properties = new HashMap<String, String>() {{
            put("source", source.getKey().toString());
            put("destination", destination.getKey().toString());
        }};

        return getRecordByProperty(properties);
    }

    public List<SimpleEntry<String, String>> getExchangeDisplayData() {
        var result = new ArrayList<SimpleEntry<String, String>>();
        for (var value : data.values()) {
            if (!value.getSource().equals(value.getDestination())) {
                result.add(new SimpleEntry<>(value.getSource().getCode() + " -> " + value.getDestination().getCode(), value.getRate().toString()));
            }
        }
        return result;
    }
}
