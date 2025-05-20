package com.bank;

import java.io.Serializable;

public class Account implements Serializable {
    private Currency currency;
    private User user;
    private String IBAN;
    private Double amount;
    private String firstName, lastName;

    // constructor for existing account
    public Account(Currency currency, User user, String IBAN, Double amount, String firstName, String lastName) {
        this.currency = currency;
        this.user = user;
        this.IBAN = IBAN;
        this.amount = amount;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // constructor for new account
    public Account(Currency currency, User user, String firstName, String lastName) {
        this.currency = currency;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.IBAN = user.getCountry().generateIBAN();
        this.amount = 0.0;
    }

    public Account() {
        this.amount = 0.0;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Account [IBAN=" + IBAN + ", currency=" + currency + ", amount=" + amount + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Account account)) return false;
        return IBAN.equals(account.IBAN);
    }

    @Override
    public int hashCode() {
        return IBAN.hashCode();
    }
}

