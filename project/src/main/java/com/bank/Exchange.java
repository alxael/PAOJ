package com.bank;

import java.io.Serializable;

public class Exchange implements Serializable {
    private Currency source;

    private Currency destination;

    private Double rate;

    public Exchange(Currency source, Currency destination, Double rate) {
        this.source = source;
        this.destination = destination;
        this.rate = rate;
    }

    public Currency getSource() {
        return source;
    }

    public void setSource(Currency source) {
        this.source = source;
    }

    public Currency getDestination() {
        return destination;
    }

    public void setDestination(Currency destination) {
        this.destination = destination;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "Exchange [source=" + source.getCode() + ", destination=" + destination.getCode() + ", rate=" + rate + "]";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Exchange exchange)) return false;

        return source.equals(exchange.source) && destination.equals(exchange.destination);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        return result;
    }
}
