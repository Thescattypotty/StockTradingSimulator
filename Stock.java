
class Stock {
    private String symbol;
    private double price;
    private double volatility;

    private double previousPrice;
    private double dayHigh;
    private double dayLow;


    public Stock(String symbol, double initialPrice, double volatility) {
        this.symbol = symbol;
        this.price = initialPrice;
        this.volatility = volatility;
        this.previousPrice = initialPrice;
        this.dayHigh = initialPrice;
        this.dayLow = initialPrice;
    }

    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    
    public void updatePrice() {
        previousPrice = price;
        double change = (Math.random() - 0.5) * volatility;
        price = Math.max(0.01, price * (1 + change));
        dayHigh = Math.max(dayHigh, price);
        dayLow = Math.min(dayLow, price);
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public double getDayHigh() {
        return dayHigh;
    }

    public double getDayLow() {
        return dayLow;
    }
}