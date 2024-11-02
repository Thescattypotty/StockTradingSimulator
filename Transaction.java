class Transaction {
    private String symbol;
    private int shares;
    private double price;
    private long timestamp;
    private ETransactionType type;

    public Transaction(String symbol, int shares, double price, ETransactionType type) {
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %d shares of %s at $%.2f",
                new java.util.Date(timestamp), type, shares, symbol, price);
    }
}