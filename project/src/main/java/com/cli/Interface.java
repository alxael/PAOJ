package com.cli;

import com.bank.User;
import com.database.DatabaseManager;

import java.io.Console;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Interface {
    private final DatabaseManager manager;
    private final Console console;
    private final Map<Command, Function<Void, Void>> commandMapping;

    private SimpleEntry<Long, User> authenticatedUser;
    private Boolean isRunning;


    private void clearUtility() {
        try {
            for (var lineIndex = 0; lineIndex < 100; lineIndex++) {
                console.printf("\n");
            }
        } catch (Exception ex) {
            console.printf("Error clearing the console!");
        }

    }

    private String getInput() throws IOException {
        return console.readLine();
    }

    private Void clear(Void unused) {
        clearUtility();
        return unused;
    }

    private Void help(Void unused) {
        var noAuthenticationRequiredCommands = new ArrayList<Command>();
        var authenticationRequiredCommands = new ArrayList<Command>();

        for (var command : commandMapping.keySet()) {
            if (command.getAuthenticationRequired()) {
                authenticationRequiredCommands.add(command);
            } else {
                noAuthenticationRequiredCommands.add(command);
            }
        }
        clear(unused);

        console.printf("Commands that don't require authentication:\n");
        for (var command : noAuthenticationRequiredCommands) {
            console.printf("%s\n", command.getFormattedDescription());
        }

        console.printf("\n");

        console.printf("Commands that require authentication:\n");
        for (var command : authenticationRequiredCommands) {
            console.printf("%s\n", command.getFormattedDescription());
        }
        return unused;
    }

    private Void exit(Void unused) {
        clearUtility();
        isRunning = false;
        return unused;
    }

    private Void viewExchange(Void unused) {
        try {
            var exchanges = manager.getExchangeEntity().getExchangeDisplayData();
            for (var exchange : exchanges) {
                console.printf("%s at %s\n", exchange.getKey(), exchange.getValue());
            }
        } catch (Exception ignoredException) {
            // log error here
        }
        return unused;
    }

    public Interface(String url, String username, String password, String initializationFilePath) throws SQLException {
        this.manager = new DatabaseManager(DriverManager.getConnection(url, username, password), initializationFilePath);
        this.console = System.console();
        this.authenticatedUser = new SimpleEntry<>(-1L, null);

        this.commandMapping = new HashMap<>();
        commandMapping.put(new Command("clear", "clear terminal", false), this::clear);
        commandMapping.put(new Command("exit", "exit application", false), this::exit);
        commandMapping.put(new Command("help", "list application commands", false), this::help);

        commandMapping.put(new Command("view-exchange", "view current exchange rates", false), this::viewExchange);
    }

    public void start() {
        isRunning = true;

        clearUtility();

        console.printf("Welcome to Useless Bank! You can view the available commands by typing 'help' below.\n");

        while (isRunning) {
            var input = console.readLine("> ");

            var commandFound = false;
            for (var commandEntry : commandMapping.entrySet()) {
                if (input.equals(commandEntry.getKey().getName())) {
                    if (authenticatedUser.getKey() == -1L && commandEntry.getKey().getAuthenticationRequired()) {
                        console.printf("You must be authenticated to use this functionality!");
                        commandFound = true;
                        break;
                    }
                    console.printf("\n");
                    try {
                        commandEntry.getValue().apply(null);
                    } catch (Exception ex) {
                        console.printf("%s\n", ex.getMessage());
                    }
                    console.printf("\n");
                    commandFound = true;
                    break;
                }
            }

            if (!commandFound) {
                console.printf("Command not found!\n");
            }
        }
    }
}
