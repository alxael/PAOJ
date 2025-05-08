package com.cli;

public class Command {
    private final String name;
    private final String description;
    private final Boolean authenticationRequired;

    public Command(String name, String description, Boolean authenticationRequired) {
        this.name = name;
        this.description = description;
        this.authenticationRequired = authenticationRequired;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getAuthenticationRequired() {
        return authenticationRequired;
    }

    public String getFormattedDescription() {
        String result = "";
        result += authenticationRequired ? "*" : " ";
        result += "[" + name + "] - ";
        result += description;
        return result;
    }
}
