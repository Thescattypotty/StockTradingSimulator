import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TradingTerminal implements MarketListener {
    private final MarketSimulator market;
    private final Portfolio portfolio;
    private final Scanner scanner;
    private volatile boolean showPriceUpdates = true;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BOLD = "\u001B[1m";

    private final Map<String, Long> lastUpdateTime = new HashMap<>();
    private static final long UPDATE_THROTTLE_MS = 1000;

    public TradingTerminal() {
        this.market = new MarketSimulator();
        this.portfolio = new Portfolio(10000.0);
        this.scanner = new Scanner(System.in);
        market.addListener(this);
    }

    public void start() {
        Thread marketThread = new Thread(market);
        marketThread.start();

        System.out.println("Welcome to the Stock Trading Simulator!");
        System.out.println("Available commands: buy, sell, portfolio, history, prices, show , hide, exit");

        while (true) {
            System.out.print("\nEnter command: ");
            String command = scanner.nextLine().toLowerCase();

            switch (command) {
                case "buy":
                    executeBuyOrder();
                    break;
                case "sell":
                    executeSellOrder();
                    break;
                case "portfolio":
                    displayPortfolio();
                    break;
                case "history":
                    displayTransactionHistory();
                    break;
                case "prices":
                    displayCurrentPrices();
                    break;
                case "show":
                    showPriceUpdates = true;
                    System.out.println("Price updates enabled.");
                    break;
                case "hide":
                    showPriceUpdates = false;
                    System.out.println("Price updates disabled.");
                    break;
                case "exit":
                    market.stop();
                    System.out.println("Thank you for trading!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Unknown command. Try again.");
            }
        }
    }

    private void executeBuyOrder() {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.getStock(symbol);

        if (stock == null) {
            System.out.println("Invalid stock symbol.");
            return;
        }

        System.out.print("Enter number of shares: ");
        int shares = Integer.parseInt(scanner.nextLine());

        if (portfolio.buyStock(stock, shares)) {
            System.out.printf("Successfully bought %d shares of %s at $%.2f\n",
                    shares, symbol, stock.getPrice());
        } else {
            System.out.println("Insufficient funds for this purchase.");
        }
    }

    private void executeSellOrder() {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.getStock(symbol);

        if (stock == null) {
            System.out.println("Invalid stock symbol.");
            return;
        }

        System.out.print("Enter number of shares: ");
        int shares = Integer.parseInt(scanner.nextLine());

        if (portfolio.sellStock(stock, shares)) {
            System.out.printf("Successfully sold %d shares of %s at $%.2f\n",
                    shares, symbol, stock.getPrice());
        } else {
            System.out.println("Insufficient shares for this sale.");
        }
    }

    private void displayPortfolio() {
        System.out.println("\nCurrent Portfolio:");
        System.out.printf("Cash: $%.2f\n", portfolio.getCash());
        System.out.println("\nHoldings:");

        Map<String, Integer> holdings = portfolio.getHoldings();
        if (holdings.isEmpty()) {
            System.out.println("No stocks owned.");
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                Stock stock = market.getStock(entry.getKey());
                if (stock != null) {
                    double value = stock.getPrice() * entry.getValue();
                    System.out.printf("%s: %d shares (Current value: $%.2f)\n",
                            entry.getKey(), entry.getValue(), value);
                }
            }
        }
    }

    private void displayTransactionHistory() {
        System.out.println("\nTransaction History:");
        List<Transaction> history = portfolio.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : history) {
                System.out.println(t);
            }
        }
    }

    private void displayCurrentPrices() {
        System.out.println("\nCurrent Stock Prices:");
        for (String symbol : Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN")) {
            Stock stock = market.getStock(symbol);
            if (stock != null) {
                System.out.printf("%s: $%.2f\n", symbol, stock.getPrice());
            }
        }
    }

    @Override
    public void onPriceUpdate(Stock stock) {
        
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTime.get(stock.getSymbol());
        if (lastUpdate != null && currentTime - lastUpdate < UPDATE_THROTTLE_MS) {
            return;
        }
        lastUpdateTime.put(stock.getSymbol(), currentTime);

        double priceChange = stock.getPrice() - stock.getPreviousPrice();
        double percentageChange = (priceChange / stock.getPreviousPrice()) * 100;

        String movement;
        String colorCode;
        if (priceChange > 0) {
            movement = "▲";
            colorCode = ANSI_GREEN;
        } else if (priceChange < 0) {
            movement = "▼";
            colorCode = ANSI_RED;
        } else {
            movement = "═";
            colorCode = ANSI_YELLOW;
        }

        StringBuilder update = new StringBuilder("\r");
        update.append(ANSI_BOLD)
                .append(stock.getSymbol())
                .append(ANSI_RESET)
                .append(" ")
                .append(colorCode)
                .append(String.format("$%.2f %s ", stock.getPrice(), movement))
                .append(String.format("(%+.2f%%)", percentageChange))
                .append(ANSI_RESET)
                .append(" H: ")
                .append(String.format("%.2f", stock.getDayHigh()))
                .append(" L: ")
                .append(String.format("%.2f", stock.getDayLow()));

        Map<String, Integer> holdings = portfolio.getHoldings();
        int shares = holdings.getOrDefault(stock.getSymbol(), 0);
        if (shares > 0) {
            double positionValue = shares * stock.getPrice();
            double positionChange = shares * priceChange;
            update.append(colorCode)
                    .append(String.format(" | Position: %d shares ($%.2f) %+.2f",
                            shares, positionValue, positionChange))
                    .append(ANSI_RESET);
        }

        if (!showPriceUpdates) {
            return;
        }
        System.out.println(update.toString());

        if (stock.getSymbol().equals("AMZN")) {
            System.out.println("-".repeat(80));
        }
    }
}