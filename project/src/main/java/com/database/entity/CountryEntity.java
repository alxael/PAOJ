package com.database.entity;

import com.bank.Country;
import com.database.Entity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class CountryEntity extends Entity<Long, Country> {
    @Override
    protected SimpleEntry<Long, Country> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var name = resultSet.getString("name");
        var code = resultSet.getString("code");
        var pattern = resultSet.getString("ibanpattern");

        var country = new Country(name, code, pattern);
        return new SimpleEntry<>(id, country);
    }

    public CountryEntity(Connection connection) {
        super("countries", connection);
    }

    public SimpleEntry<Long, Country> getCountryFromCode(String code) throws SQLException {
        return getRecordByProperty("code", code);
    }

    public List<SimpleEntry<String, String>> getCountryDisplayData() {
        var result = new ArrayList<SimpleEntry<String, String>>();
        for (var value : data.values()) {
            result.add(new SimpleEntry<>(value.getCode(), value.getName()));
        }
        return result;
    }
}
