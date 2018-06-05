package model.data;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.TickerStatistics;
import model.DataIdentifier;

import java.util.HashMap;
import java.util.List;

public class MindData {

    public HashMap<DataIdentifier, List<Candlestick>> candlestickData;
    public HashMap<DataIdentifier, TickerStatistics> lastPriceData;
    public HashMap<DataIdentifier, Double> candlestickIntAvgData;

    public MindData() {
        candlestickData = new HashMap<DataIdentifier, List<Candlestick>>();
        lastPriceData = new HashMap<DataIdentifier, TickerStatistics>();
        candlestickIntAvgData = new HashMap<DataIdentifier, Double>();
    }

    public HashMap<DataIdentifier, List<Candlestick>> getCandlestickData() {
        return candlestickData;
    }

    public void setCandlestickData(HashMap<DataIdentifier, List<Candlestick>> candlestickData) {
        this.candlestickData = candlestickData;
    }

    public HashMap<DataIdentifier, TickerStatistics> getLastPriceData() {
        return lastPriceData;
    }

    public void setLastPriceData(HashMap<DataIdentifier, TickerStatistics> lastPriceData) {
        this.lastPriceData = lastPriceData;
    }

    public HashMap<DataIdentifier, Double> getCandlestickIntAvgData() {
        return candlestickIntAvgData;
    }

    public void setCandlestickIntAvgData(HashMap<DataIdentifier, Double> candlestickIntAvgData) {
        this.candlestickIntAvgData = candlestickIntAvgData;
    }
}