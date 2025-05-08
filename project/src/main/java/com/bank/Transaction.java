package com.bank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private Account inbound;
    private Account outbound;
    private double amount;
    private LocalDateTime date;

    public Transaction(Account inbound, Account outbound, double amount, LocalDateTime date) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.amount = amount;
        this.date = date;
    }

    public Transaction() {
        this.date = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Account getInbound() {
        return inbound;
    }

    public void setInbound(Account inbound) {
        this.inbound = inbound;
    }

    public Account getOutbound() {
        return outbound;
    }

    public void setOutbound(Account outbound) {
        this.outbound = outbound;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date.format(formatter);
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Transaction [inbound=" + inbound.getIBAN() + ", outbound=" + outbound.getIBAN() + ", amount=" + amount + ", date=" + getDateString() + "]";
    }
}
