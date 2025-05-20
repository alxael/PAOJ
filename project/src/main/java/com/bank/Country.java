package com.bank;

import java.io.Serializable;
import java.util.Random;

import static java.lang.Character.isDigit;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isAlphabetic;

public class Country implements Serializable {
    private String name;

    private String code;

    private String IBANPattern;

    public Country(String name, String code, String IBANPattern) {
        if (name.isEmpty() || code.isEmpty() || IBANPattern.isEmpty()) {
            throw new IllegalArgumentException("All constructor parameters must be non-empty.");
        }
        if (IBANPattern.length() < 15 || IBANPattern.length() > 34) {
            throw new IllegalArgumentException("IBAN pattern must be between 15 and 34 characters.");
        }

        this.name = name;
        this.code = code;
        this.IBANPattern = IBANPattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIBANPattern() {
        return IBANPattern;
    }

    public void setIBANPattern(String IBANPattern) {
        this.IBANPattern = IBANPattern;
    }

    private boolean IBANMatchesPattern(String IBAN) {
        // check country code
        String countryCode = IBAN.substring(0, 2);
        if (!countryCode.equals(this.IBANPattern.substring(0, 2))) {
            return false;
        }

        // check checksum digits
        if (!isDigit(IBAN.charAt(2)) || !isDigit(IBAN.charAt(3))) {
            return false;
        }

        // check length
        var truncatedIBAN = IBAN.substring(4);
        if (truncatedIBAN.length() != IBANPattern.length()) {
            return false;
        }

        // check pattern
        for (int index = 0; index < truncatedIBAN.length(); index++) {
            var currentPatternCharacter = truncatedIBAN.charAt(index);
            var currentIBANCharacter = truncatedIBAN.charAt(index);

            if (currentPatternCharacter == 'a' && !isUpperCase(currentIBANCharacter)) {
                return false;
            }
            if (currentPatternCharacter == 'n' && !isDigit(currentIBANCharacter)) {
                return false;
            }
            if (currentPatternCharacter == 'c' && (!isAlphabetic(currentIBANCharacter) && !isDigit(currentIBANCharacter))) {
                return false;
            }
        }

        return true;
    }

    private Long getChecksum(String truncatedIBAN) {
        var numberString = new StringBuilder();
        for (int index = 0; index < truncatedIBAN.length(); index++) {
            var currentChar = truncatedIBAN.charAt(index);
            if (isDigit(currentChar)) {
                numberString.append(currentChar);
            }
            if (isUpperCase(currentChar)) {
                numberString.append((int) currentChar - (int) 'A');
            }
            if (isLowerCase(currentChar)) {
                numberString.append((int) currentChar - (int) 'a');
            }
        }

        int segmentStart = 0;
        int step = 9;
        String prepended = "";
        long number;

        while (segmentStart < numberString.length() - step) {
            number = Long.parseLong(numberString.substring(segmentStart, segmentStart + step));
            long remainder = number % 97;
            prepended = Long.toString(remainder);
            if (remainder < 10) {
                prepended = "0" + prepended;
            }
            segmentStart += step;
            step = 7;
        }

        number = Long.parseLong(prepended + numberString.substring(segmentStart));
        return number;
    }

    @Override
    public String toString() {
        return "Country [name=" + name + ", code=" + code + ", IBANPattern=" + IBANPattern + "]";
    }

    public boolean isIBANValid(String IBAN) {
        if (!IBANMatchesPattern(IBAN)) {
            return false;
        }

        IBAN = IBAN + IBAN.substring(0, 4);
        IBAN = IBAN.substring(4);

        long checksum = getChecksum(IBAN);
        return (checksum % 97 == 1);
    }

    public String generateIBAN() {
        var random = new Random();

        StringBuilder partialIBAN = new StringBuilder();
        for (int index = 0; index < IBANPattern.length(); index++) {
            switch (IBANPattern.charAt(index)) {
                case 'a':
                    partialIBAN.append((char) ('A' + random.nextInt(26)));
                    break;
                case 'n':
                    partialIBAN.append((char) ('0' + random.nextInt(10)));
                    break;
                case 'c':
                    partialIBAN.append((char) ('a' + random.nextInt(26)));
                    break;
            }
        }

        String prefixAddedIBAN = partialIBAN + code + "00";
        String checkDigits = String.valueOf(98 - getChecksum(prefixAddedIBAN) % 97);
        if (checkDigits.length() < 2) {
            checkDigits = "0" + checkDigits;
        }

        return code + checkDigits + partialIBAN;
    }
}
