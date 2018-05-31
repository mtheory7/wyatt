package model;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MindData {

  public HashMap<CandlestickInterval, List<Candlestick>> candlestickData;

  public MindData() {
    candlestickData = new HashMap<CandlestickInterval, List<Candlestick>>();
  }

  public Map<CandlestickInterval, List<Candlestick>> getCandlestickData() {
    return candlestickData;
  }

  public void setCandlestickData(HashMap<CandlestickInterval, List<Candlestick>> candlestickData) {
    this.candlestickData = candlestickData;
  }
}
    /*System.out.println("Time interval: 1 minute   =====  Number of candlesticks fetched: " + candles1minute.size() + " :: Average: $" + candles1minuteAvg);
    System.out.println("Time interval: 3 minutes  =====  Number of candlesticks fetched: " + candles3minute.size() + " :: Average: $" + candles3minuteAvg);
    System.out.println("Time interval: 5 minutes  =====  Number of candlesticks fetched: " + candles5minute.size() + " :: Average: $" + candles5minuteAvg);
    System.out.println("Time interval: 15 minutes =====  Number of candlesticks fetched: " + candles15minute.size() + " :: Average: $" + candles15minuteAvg);
    System.out.println("Time interval: 1/2 hour   =====  Number of candlesticks fetched: " + candles30minute.size() + " :: Average: $" + candles30minuteAvg);
    System.out.println("Time interval: 1 hour     =====  Number of candlesticks fetched: " + candles1hour.size() + " :: Average: $" + candles1hourAvg);
    System.out.println("Time interval: 2 hours    =====  Number of candlesticks fetched: " + candles2hour.size() + " :: Average: $" + candles2hourAvg);
    System.out.println("Time interval: 4 hours    =====  Number of candlesticks fetched: " + candles4hour.size() + " :: Average: $" + candles4hourAvg);
    System.out.println("Time interval: 8 hours    =====  Number of candlesticks fetched: " + candles8hour.size() + " :: Average: $" + candles8hourAvg);
    System.out.println("\nCURRENT PRICE === $" + currentPrice);*/