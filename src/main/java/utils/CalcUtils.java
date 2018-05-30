package utils;

import com.binance.api.client.domain.market.Candlestick;

import java.util.List;

public class CalcUtils {
  public Double findAveragePrice(List<Candlestick> candlesticks) {

    if (candlesticks.size() == 0) return 0.0;

    Double average = 0.0;

    for (Candlestick stick : candlesticks) {
      average += Double.valueOf(stick.getClose());
    }

    return average/candlesticks.size();
  }
}
