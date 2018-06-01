package model;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MindData {

	public HashMap<CandlestickInterval, List<Candlestick>> candlestickData;
	public HashMap<CandlestickInterval, TickerStatistics> lastPriceData;
	public HashMap<CandlestickInterval, Double> candlestickIntAvgData;

	public MindData() {
		candlestickData = new HashMap<CandlestickInterval, List<Candlestick>>();
		lastPriceData = new HashMap<CandlestickInterval, TickerStatistics>();
		candlestickIntAvgData = new HashMap<CandlestickInterval, Double>();
	}

	public Map<CandlestickInterval, List<Candlestick>> getCandlestickData() {
		return candlestickData;
	}

	public void setCandlestickData(HashMap<CandlestickInterval, List<Candlestick>> candlestickData) {
		this.candlestickData = candlestickData;
	}

	public HashMap<CandlestickInterval, TickerStatistics> getLastPriceData() {
		return lastPriceData;
	}

	public void setLastPriceData(HashMap<CandlestickInterval, TickerStatistics> lastPriceData) {
		this.lastPriceData = lastPriceData;
	}

	public HashMap<CandlestickInterval, Double> getCandlestickIntAvgData() {
		return candlestickIntAvgData;
	}

	public void setCandlestickIntAvgData(HashMap<CandlestickInterval, Double> candlestickIntAvgData) {
		this.candlestickIntAvgData = candlestickIntAvgData;
	}
}