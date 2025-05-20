package com.database;

import com.exception.EntryDuplicateFoundException;
import com.exception.EntryNotFoundException;
import com.exception.EntrySynchronizationException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Entity<Key, Data> implements Serializable {
    protected Connection connection;
    protected Map<Key, Data> data;
    protected String table;

    protected static LocalDateTime stringToLocalDateTime(String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(date, formatter);
    }

    public Entity(String table, Connection connection) {
        this.connection = connection;
        this.table = table;
    }

    // data parsing method to be implemented
    protected abstract SimpleEntry<Key, Data> parseData(ResultSet resultSet) throws SQLException;

    // query list methods
    protected Map<Key, Data> getRecords(Query query) throws SQLException {
        var result = query.executeSelect();
        var dataResult = new HashMap<Key, Data>();
        while (result.next()) {
            var entry = parseData(result);
            dataResult.put(entry.getKey(), entry.getValue());
        }
        return dataResult;
    }

    protected Map<Key, Data> getAllRecords() throws SQLException {
        var query = new Query(connection, "SELECT * FROM :table;");
        query.setParameter("table", table, false);
        return getRecords(query);
    }

    protected Map<Key, Data> getRecordsByProperty(String property, String value) throws SQLException {
        Query query = new Query(connection, "SELECT * FROM :table WHERE :property=:value;");
        query.setParameter("table", table, false)
                .setParameter("property", property, false)
                .setParameter("value", value);
        return getRecords(query);
    }

    protected Map<Key, Data> getRecordsByProperty(Map<String, String> properties) throws SQLException {
        Query query = new Query(connection, "SELECT * FROM :table WHERE ");
        query.setParameter("table", table, false);
        int index = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propParam = "prop" + index;
            String valParam = "val" + index;

            query.append(":" + propParam + "=:" + valParam + " ");
            query.setParameter(propParam, entry.getKey(), false)
                    .setParameter(valParam, entry.getValue());

            if (++index < properties.size()) {
                query.append("AND ");
            }
        }
        query.append(";");
        return getRecords(query);
    }

    // query single methods
    protected SimpleEntry<Key, Data> getRecordByProperty(String property, String value) throws SQLException {
        Map<Key, Data> result = getRecordsByProperty(property, value);
        if (result.size() > 1) {
            throw new EntryDuplicateFoundException("Multiple entries for unique property " + property + " found in table " + table + "!");
        }
        if (result.isEmpty()) {
            throw new EntryNotFoundException("Could not find entry with property " + property + " in table " + table + "!");
        }
        var entry = result.entrySet().iterator().next();
        return new SimpleEntry<>(entry.getKey(), entry.getValue());
    }

    protected SimpleEntry<Key, Data> getRecordByProperty(Map<String, String> properties) throws SQLException {
        Map<Key, Data> result = getRecordsByProperty(properties);
        String propertiesDescription = properties.entrySet().stream()
                .map(entry -> "(" + entry.getKey() + "," + entry.getValue() + ")")
                .collect(Collectors.joining());

        if (result.size() > 1) {
            throw new EntryDuplicateFoundException("Multiple entries found for " + propertiesDescription + "found in table " + table + "!");
        }
        if (result.isEmpty()) {
            throw new EntryNotFoundException("Could not find entry for " + propertiesDescription + "in table " + table + "!");
        }
        var entry = result.entrySet().iterator().next();
        return new SimpleEntry<>(entry.getKey(), entry.getValue());
    }

    // delete operations
    public void deleteRecordsByProperty(String property, String value) throws SQLException {
        Query query = new Query(connection, "DELETE FROM :table WHERE :property=:value;");
        query.setParameter("table", table, false)
                .setParameter("property", property, false)
                .setParameter("value", value, true);
        query.execute();
        loadData();
    }

    public void deleteRecordById(Key id) throws SQLException {
        deleteRecordsByProperty("id", id.toString());
    }

    // data management
    public void loadData() throws SQLException {
        this.data = getAllRecords();
    }

    public Map<Key, Data> getData() {
        return data;
    }

    public SimpleEntry<Key, Data> getRecordById(Key id) throws SQLException {
        return getRecordById(id, false);
    }

    public SimpleEntry<Key, Data> getRecordById(Key id, boolean cached) throws SQLException {
        if (cached) {
            Data entry = data.get(id);
            if (entry == null) {
                throw new EntrySynchronizationException("Entry " + id + " not synchronized in " + table);
            }
            return new SimpleEntry<>(id, entry);
        }
        return getRecordByProperty("id", id.toString());
    }
}
