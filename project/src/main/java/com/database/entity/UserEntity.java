package com.database.entity;

import com.bank.User;
import com.database.Entity;
import com.database.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;

public class UserEntity extends Entity<Long, User> {
    private static final Logger log = LoggerFactory.getLogger(UserEntity.class);

    private final CountryEntity countryEntity;

    @Override
    protected SimpleEntry<Long, User> parseData(ResultSet resultSet) throws SQLException {
        var id = resultSet.getLong("id");
        var countryId = resultSet.getLong("country");
        var email = resultSet.getString("email");
        var firstName = resultSet.getString("firstname");
        var lastName = resultSet.getString("lastname");
        var password = resultSet.getString("password");

        var country = countryEntity.getRecordById(countryId);
        var user = new User(country.getValue(), password, lastName, firstName, email);
        return new SimpleEntry<>(id, user);
    }

    public UserEntity(Connection connection, CountryEntity countryEntity) {
        super("users", connection);
        this.countryEntity = countryEntity;
    }

    public SimpleEntry<Long, User> getUserFromEmail(String email) throws SQLException {
        return getRecordByProperty("email", email);
    }

    public SimpleEntry<Long, User> createUser(String countryCode, String email, String firstName, String lastName, String password) throws SQLException {
        var country = countryEntity.getCountryFromCode(countryCode);
        var user = new User(password, lastName, firstName, email, country.getValue());

        log.info(user.getPassword());

        var query = new Query(connection, "INSERT INTO :table VALUES (DEFAULT, :country, :email, :firstName, :lastName, :password);");
        query.setParameter("table", table, false)
                .setParameter("country", country.getKey())
                .setParameter("email", email)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("password", user.getPassword());
        query.execute();

        var entry = getUserFromEmail(email);
        data.put(entry.getKey(), entry.getValue());
        return entry;
    }
}
