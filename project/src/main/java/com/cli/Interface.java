package com.cli;

import com.bank.Account;
import com.bank.Transaction;
import com.bank.User;
import com.database.DatabaseManager;
import com.exception.InvalidBusinessLogicException;
import com.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;

public class Interface {
    private final static Logger log = LoggerFactory.getLogger(Interface.class);

    private final DatabaseManager manager;
    private final Console console;
    private final Map<Command, Function<Void, Void>> commandMapping;

    private SimpleEntry<Long, User> authenticatedUser;
    private Boolean isRunning;

    private String getInput(String message) {
        return console.readLine(message);
    }

    private String getInput() {
        return console.readLine();
    }

    private void clearUtility() {
        try {
            for (var lineIndex = 0; lineIndex < 100; lineIndex++) {
                console.printf("\n");
            }
        } catch (Exception ex) {
            console.printf("Error clearing the console!");
        }

    }

    private String emailUtility() {
        String email;
        while (true) {
            email = getInput("Email: ");
            if (email.isEmpty() || !Validator.isEmail(email)) {
                console.printf("The provided email is not valid.\n");
            } else {
                break;
            }
        }
        return email;
    }

    private String passwordUtility(Boolean validate) {
        String password;
        while (true) {
            password = Arrays.toString(console.readPassword("Password: "));
            if (validate) {
                if (password.isEmpty() || !Validator.isPasswordStrong(password)) {
                    console.printf("The provided password does not match the strength criteria.\n");
                    console.printf("Please make sure your password contains at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character.\n");
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return password;
    }

    private String countryCodeUtility() {
        console.printf("Please enter your country code. The available countries are:\n");
        var countryDisplayData = manager.getCountryEntity().getCountryDisplayData();
        for (var entry : countryDisplayData) {
            console.printf("%s: %s\n", entry.getKey(), entry.getValue());
        }
        console.printf("\n");

        var countryCodes = new HashSet<String>();
        for (var entry : countryDisplayData) {
            countryCodes.add(entry.getKey().toLowerCase());
        }

        String countryCode;
        while (true) {
            countryCode = getInput("Country code: ");
            if (countryCode.isEmpty() || !countryCodes.contains(countryCode.trim().toLowerCase())) {
                console.printf("Please enter a valid country code.\n");
            } else {
                break;
            }
        }
        return countryCode;
    }

    private String currencyCodeUtility() {
        console.printf("Please enter the currency. The available currencies are:\n");
        var currencyDisplayData = manager.getCurrencyEntity().getCurrencyDisplayData();
        for (var entry : currencyDisplayData) {
            console.printf("%s: %s\n", entry.getKey(), entry.getValue());
        }
        console.printf("\n");

        var currencyCodes = new HashSet<String>();
        for (var entry : currencyDisplayData) {
            currencyCodes.add(entry.getKey().toLowerCase());
        }

        String currencyCode;
        while (true) {
            currencyCode = getInput("Currency code: ");
            if (currencyCode.isEmpty() || !currencyCodes.contains(currencyCode.trim().toLowerCase())) {
                console.printf("Please enter a valid currency code.\n");
            } else {
                break;
            }
        }
        return currencyCode;
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

    private Void signup(Void unused) {
        if (authenticatedUser.getKey() != -1L) {
            throw new RuntimeException("An user is already logged in! Please log out first!");
        }
        var firstName = getInput("First name: ");
        var lastName = getInput("Last name: ");
        var email = emailUtility();
        var password = passwordUtility(true);
        var countryCode = countryCodeUtility();

        try {
            authenticatedUser = manager.getUserEntity().createUser(countryCode, email, firstName, lastName, password);
            console.printf("You have successfully created an account!\n");
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return unused;
    }

    private Void login(Void unused) {
        if (authenticatedUser != null) {
            throw new RuntimeException("An user is already signed in! Please log out first!");
        }
        var email = emailUtility();
        var password = passwordUtility(false);
        try {
            var user = manager.getUserEntity().getUserFromEmail(email);
            if (user.getValue().authenticate(password)) {
                authenticatedUser = user;
                console.printf("You have successfully logged in!\n");
            } else {
                console.printf("Invalid credentials!\n");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return unused;
    }

    private Void logout(Void unused) {
        authenticatedUser = null;
        console.printf("You have successfully logged out!\n");
        return unused;
    }

    private Void userInfo(Void unused) {
        console.printf("Email: %s\n", authenticatedUser.getValue().getEmail());
        console.printf("Full name: %s\n", authenticatedUser.getValue().getFullName());
        console.printf("Country: %s\n", authenticatedUser.getValue().getCountry().getName());
        return unused;
    }

    private Void deleteUser(Void unused) {
        console.printf("Are you sure you want to delete this account? [yes/no]\n");
        var confirmation = getInput("Confirmation: ");
        if (confirmation.compareTo("yes") == 0 || confirmation.compareTo("y") == 0) {
            try {
                manager.getUserEntity().deleteRecordById(authenticatedUser.getKey());
                authenticatedUser = null;
                console.printf("Account deleted successfully!\n");
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }
        return unused;
    }

    private Void addAccount(Void unused) {
        var firstName = getInput("Account holder's first name: ");
        var lastName = getInput("Account holder's last name: ");
        var currencyCode = currencyCodeUtility();
        try {
            manager.getAccountEntity().createAccount(currencyCode, authenticatedUser.getKey(), firstName, lastName);
            console.printf("You have successfully created a bank account!\n");
        } catch (SQLException exception) {
            log.error(exception.getMessage());
            throw new RuntimeException(exception);
        }
        return unused;
    }

    private Void viewAccounts(Void unused) {
        try {
            var accounts = manager.getAccountEntity().getUserAccounts(authenticatedUser.getKey());
            for (var account : accounts.values()) {
                console.printf("Account %s\n\n", account.getIBAN());
                console.printf("IBAN: %s\n", account.getIBAN());
                console.printf("Amount: %s\n", account.getAmount());
                console.printf("Currency: %s\n", account.getCurrency().getCode());
                console.printf("Holder's full name: %s\n\n", account.getFullName());
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
        return unused;
    }

    private Void deleteAccount(Void unused) {
        viewAccounts(unused);

        console.printf("\n");
        var IBAN = getInput("IBAN: ");

        try {
            var account = manager.getAccountEntity().getAccountFromIBAN(IBAN);
            var accounts = manager.getAccountEntity().getUserAccounts(authenticatedUser.getKey());

            if (!accounts.containsKey(account.getKey())) {
                throw new InvalidBusinessLogicException("You can not delete this account!");
            }

            manager.getAccountEntity().deleteAccount(account.getKey());
            console.printf("You have successfully deleted your bank account!\n");
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return unused;
    }

    private Void addTransaction(Void unused) {
        console.printf("Please select the account from which you want to make the transaction:\n");
        viewAccounts(unused);
        console.printf("\n");

        var outboundIBAN = getInput("Your IBAN: ");
        var inboundIBAN = getInput("Destination IBAN: ");
        var amountString = getInput("Amount: ");
        var amount = Double.parseDouble(amountString);

        if (outboundIBAN.isEmpty() || inboundIBAN.isEmpty()) {
            throw new RuntimeException("The IBAN you enter must not be empty.");
        }

        if (outboundIBAN.compareTo(inboundIBAN) == 0) {
            throw new RuntimeException("You can not make a transfer from an account to the same account.");
        }

        try {
            manager.getTransactionEntity().createTransaction(authenticatedUser.getKey(), inboundIBAN, outboundIBAN, amount);
            console.printf("Transaction successfully registered!\n");
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
        return unused;
    }

    private Void viewTransactions(Void unused) {
        console.printf("Please select an account from which you want to see transactions:\n");
        viewAccounts(unused);
        console.printf("\n");

        var IBAN = getInput("IBAN: ");

        try {
            var matches = false;
            var accounts = manager.getAccountEntity().getUserAccounts(authenticatedUser.getKey());
            Entry<Long, Account> userAccount = null;
            for (var account : accounts.entrySet()) {
                if (account.getValue().getIBAN().compareTo(IBAN) == 0) {
                    matches = true;
                    userAccount = account;
                    break;
                }
            }
            if (!matches) {
                throw new InvalidBusinessLogicException("IBAN does not exist, or it is not associated with one of your accounts.");
            }

            var transactions = manager.getTransactionEntity().getAccountTransactions(userAccount.getKey());

            var inboundTransactions = new ArrayList<SimpleEntry<String, String>>();
            for (var transaction : transactions.getKey().values()) {
                inboundTransactions.add(new SimpleEntry<>(transaction.getDateString(),
                        "From " + transaction.getOutbound().getIBAN() +
                                " received " + transaction.getAmount() +
                                " " + transaction.getOutbound().getCurrency().getCode() +
                                " on " + transaction.getDateString()));
            }

            var outboundTransactions = new ArrayList<SimpleEntry<String, String>>();
            for (var transaction : transactions.getValue().values()) {
                outboundTransactions.add(new SimpleEntry<>(transaction.getDateString(),
                        "To " + transaction.getOutbound().getIBAN() +
                                " sent " + transaction.getAmount() +
                                " " + transaction.getOutbound().getCurrency().getCode() +
                                " on " + transaction.getDateString()));
            }

            console.printf("\n");
            if (!inboundTransactions.isEmpty()) {
                inboundTransactions.sort(Entry.comparingByKey());
                console.printf("Inbound transactions:\n");
                for (var inboundTransaction : inboundTransactions) {
                    console.printf("%s\n", inboundTransaction.getValue());
                }
                console.printf("\n");
            }
            if (!outboundTransactions.isEmpty()) {
                outboundTransactions.sort(Entry.comparingByKey());
                console.printf("Outbound transactions:\n");
                for (var outboundTransaction : outboundTransactions) {
                    console.printf("%s\n", outboundTransaction.getValue());
                }
                console.printf("\n");
            }

            if (inboundTransactions.isEmpty() && outboundTransactions.isEmpty()) {
                console.printf("There are no transactions associated with this account.\n");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }


        return unused;
    }

    private Void viewExchange(Void unused) {
        try {
            var exchanges = manager.getExchangeEntity().getExchangeDisplayData();
            for (var exchange : exchanges) {
                console.printf("%s at %s\n", exchange.getKey(), exchange.getValue());
            }
        } catch (Exception exception) {
            log.error("Could not get exchange display data: {}", exception.getMessage());
        }
        return unused;
    }

    public Interface(String url, String username, String password, String initializationFilePath) throws SQLException {
        this.manager = new DatabaseManager(DriverManager.getConnection(url, username, password), initializationFilePath);
        this.console = System.console();
        this.authenticatedUser = null;

        this.commandMapping = new HashMap<>();
        commandMapping.put(new Command("clear", "clear terminal", false), this::clear);
        commandMapping.put(new Command("exit", "exit application", false), this::exit);
        commandMapping.put(new Command("help", "list application commands", false), this::help);

        commandMapping.put(new Command("signup", "create an account", false), this::signup);
        commandMapping.put(new Command("login", "log into an existing account", false), this::login);
        commandMapping.put(new Command("logout", "log out of your account", true), this::logout);
        commandMapping.put(new Command("user-info", "view your account's information", true), this::userInfo);
        commandMapping.put(new Command("user-delete", "delete your account", true), this::deleteUser);

        commandMapping.put(new Command("add-account", "create a new account", true), this::addAccount);
        commandMapping.put(new Command("view-accounts", "view your accounts", true), this::viewAccounts);
        commandMapping.put(new Command("delete-account", "delete your account", true), this::deleteAccount);

        commandMapping.put(new Command("add-transaction", "create a new transaction", true), this::addTransaction);
        commandMapping.put(new Command("view-transactions", "view all transactions from an account", true), this::viewTransactions);
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
                    if (authenticatedUser == null && commandEntry.getKey().getAuthenticationRequired()) {
                        console.printf("You must be authenticated to use this functionality!\n");
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
