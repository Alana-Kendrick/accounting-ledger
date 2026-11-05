package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    public static void loadTransactions(String fileName) {
        try {
            File file = new File(fileName);
            if (file.createNewFile()) {
                System.out.println("Created new file");
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] splitTransaction = line.split("\\|");
                LocalDate date = LocalDate.parse(splitTransaction[0], DATE_FMT);
                LocalTime time = LocalTime.parse(splitTransaction[1], TIME_FMT);
                String description = splitTransaction[2];
                String vendor = splitTransaction[3];
                double amount = Double.parseDouble(splitTransaction[4]);

                Transaction transaction = new Transaction(date, time, description, vendor, amount);
                transactions.add(transaction);
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    private static void addDeposit(Scanner scanner) {
        System.out.println("New Deposit");
        System.out.println("___________________");
        System.out.print("Date and time of deposit (yyyy-MM-dd HH:mm:ss): ");
        String dateTime = scanner.nextLine();
        LocalDate date = LocalDate.parse(dateTime.substring(0, 10).trim(), DATE_FMT);
        LocalTime time = LocalTime.parse(dateTime.substring(11).trim(), TIME_FMT);
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();

        boolean positive = false;
        double amount;
        do {
            System.out.print("Amount: $");
            amount = scanner.nextDouble();
            if (amount >= 0) {
                positive = true;
            } else {
                System.out.println("Invalid input. Enter a positive amount.");
            }
        } while (!positive);

        Transaction transaction = new Transaction(date, time, description, vendor, amount);
        transactions.add(transaction);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(transaction.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }

        System.out.println("Your deposit has successfully been added to your ledger.");
    }

    private static void addPayment(Scanner scanner) {
        System.out.println("New Payment");
        System.out.println("___________________");
        System.out.print("Date and time of payment (yyyy-MM-dd HH:mm:ss): ");
        String dateTime = scanner.nextLine();
        LocalDate date = LocalDate.parse(dateTime.substring(0, 10).trim(), DATE_FMT);
        LocalTime time = LocalTime.parse(dateTime.substring(11).trim(), TIME_FMT);
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();

        boolean positive = false;
        double amount;
        do {
            System.out.print("Amount: $");
            amount = scanner.nextDouble();
            if (amount >= 0) {
                positive = true;
                amount *= -1;
            } else {
                System.out.println("Invalid input. Enter a positive amount.");
            }
        } while (!positive);

        Transaction transaction = new Transaction(date, time, description, vendor, amount);
        transactions.add(transaction);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(transaction.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }

        System.out.println("Your payment has successfully been added to your ledger.");
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayTransactions('A');
                case "D" -> displayTransactions('D');
                case "P" -> displayTransactions('P');
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void printTransactions(Transaction transaction) {
        System.out.printf("%9tF | %8tT | %35s | %25s | %10.2f%n",
                transaction.getDate(),
                transaction.getTime(),
                transaction.getDescription(),
                transaction.getVendor(),
                transaction.getAmount()
        );
    }

    private static void displayTransactions(char input) {
        // Sort arraylist from newest to oldest here
        // Already tried using Collections.reverse(), but that did not produce the desired result

        System.out.printf(" %9s | %8s | %35s | %25s | %10s%n", "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("----------------------------------------------------------------------------------------------------");
        for (Transaction transaction : transactions) {
            switch (input) {
                case 'A':
                    printTransactions(transaction);
                    break;
                case 'D':
                    if (transaction.getAmount() > 0) {
                        printTransactions(transaction);
                    }
                    break;
                case 'P':
                    if (transaction.getAmount() < 0) {
                        printTransactions(transaction);
                    }
            }

        }
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {/* TODO – month-to-date report */ }
                case "2" -> {/* TODO – previous month report */ }
                case "3" -> {/* TODO – year-to-date report   */ }
                case "4" -> {/* TODO – previous year report  */ }
                case "5" -> {/* TODO – prompt for vendor then report */ }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }
}