package com.database;

import com.exception.IdentifierNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query {
    private static final Logger log = LoggerFactory.getLogger(Query.class);

    private final Connection connection;
    private String query;

    public Query(Connection connection, String query) {
        this.connection = connection;
        this.query = query;
    }

    public Query(Connection connection) {
        this.connection = connection;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public <T> Query setParameter(String identifier, T argument) {
        return setParameter(identifier, argument, true);
    }

    public <T> Query setParameter(String identifier, T argument, boolean addQuotes) {
        var argumentString = argument.toString();
        var identifierString = ":" + identifier;
        var parameterString = addQuotes ? ('\'' + argumentString + '\'') : argumentString;
        if (!query.contains(identifierString)) {
            throw new IdentifierNotFoundException(identifierString);
        }
        query = query.replace(identifierString, parameterString);
        return this;
    }

    public Query append(String value) {
        query = query + value;
        return this;
    }

    public ResultSet executeSelect() throws SQLException {
        var statement = connection.createStatement();
        log.info("Executing select query: {}", query);
        return statement.executeQuery(query);
    }

    public void execute() throws SQLException {
        var statement = connection.createStatement();
        log.info("Executing query: {}", query);
        statement.executeUpdate(query);
    }
}
