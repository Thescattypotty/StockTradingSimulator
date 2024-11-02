import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Portfolio {
    private Map<String, Integer> holdings = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();
    private double cash;

    public Portfolio(double initialCash) {
        this.cash = initialCash;
    }

    public synchronized boolean buyStock(Stock stock, int shares) {
        double cost = stock.getPrice() * shares;
        if (cost > cash)
            return false;

        cash -= cost;
        holdings.merge(stock.getSymbol(), shares, Integer::sum);
        transactions.add(new Transaction(stock.getSymbol(), shares, stock.getPrice(), ETransactionType.BUY));
        return true;
    }

    public synchronized boolean sellStock(Stock stock, int shares) {
        int currentShares = holdings.getOrDefault(stock.getSymbol(), 0);
        if (currentShares < shares)
            return false;

        cash += stock.getPrice() * shares;
        holdings.put(stock.getSymbol(), currentShares - shares);
        transactions
                .add(new Transaction(stock.getSymbol(), shares, stock.getPrice(), ETransactionType.SELL));
        return true;
    }

    public double getCash() {
        return cash;
    }

    public Map<String, Integer> getHoldings() {
        return new HashMap<>(holdings);
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }
}