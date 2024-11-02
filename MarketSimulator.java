import java.util.ArrayList;
import java.util.List;


class MarketSimulator implements Runnable {
    private volatile boolean running = true;
    private List<Stock> stocks = new ArrayList<>();
    private List<MarketListener> listeners = new ArrayList<>();

    public MarketSimulator() {
        stocks.add(new Stock("AAPL", 150.0, 0.02));
        stocks.add(new Stock("GOOGL", 2800.0, 0.015));
        stocks.add(new Stock("MSFT", 280.0, 0.018));
        stocks.add(new Stock("AMZN", 3300.0, 0.025));
    }

    public void addListener(MarketListener listener) {
        listeners.add(listener);
    }

    public Stock getStock(String symbol) {
        return stocks.stream()
                .filter(s -> s.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run() {
        while (running) {
            for (Stock stock : stocks) {
                stock.updatePrice();
                notifyListeners(stock);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void notifyListeners(Stock stock) {
        for (MarketListener listener : listeners) {
            listener.onPriceUpdate(stock);
        }
    }

    public void stop() {
        running = false;
    }
}