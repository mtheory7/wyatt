package model.data;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import model.DataIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionData {
	public static Double buyBackAfterThisPercentage = 0.994;
	public Double targetPrice;
	private List<AverageData> averageData;
	private List<Double> targetPrices;
	private Map<CandlestickInterval, List<Candlestick>> candleMap;

	public PredictionData() {
		averageData = new ArrayList<AverageData>();
		targetPrices = new ArrayList<Double>();
		candleMap = new HashMap<CandlestickInterval, List<Candlestick>>();
	}

	public void executeThoughtProcess(MindData mindData) {
		for (HashMap.Entry<DataIdentifier, List<Candlestick>> entry : mindData.getCandlestickData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				candleMap.put(CandlestickInterval.ONE_MINUTE, entry.getValue());
			}
			if (entry.getKey().getInterval() == CandlestickInterval.THREE_MINUTES
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				candleMap.put(CandlestickInterval.THREE_MINUTES, entry.getValue());
			}
			if (entry.getKey().getInterval() == CandlestickInterval.FIVE_MINUTES
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				candleMap.put(CandlestickInterval.FIVE_MINUTES, entry.getValue());
			}
			if (entry.getKey().getInterval() == CandlestickInterval.FIFTEEN_MINUTES
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				candleMap.put(CandlestickInterval.FIFTEEN_MINUTES, entry.getValue());
			}
		}
		for (HashMap.Entry<CandlestickInterval, List<Candlestick>> entry : candleMap.entrySet()) {
			averageData.add(calculateAverageData(entry));
		}
	}

	private AverageData calculateAverageData(HashMap.Entry<CandlestickInterval, List<Candlestick>> entry) {
		AverageData averageData = new AverageData();

		return averageData;
	}
}
