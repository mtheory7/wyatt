package com.mtheory7.wyatt.model;

import com.binance.api.client.domain.market.CandlestickInterval;

public class DataIdentifier {
  private CandlestickInterval interval;
  private String ticker;

  public DataIdentifier(CandlestickInterval interval, String ticker) {
    this.interval = interval;
    this.ticker = ticker;
  }

  public CandlestickInterval getInterval() {
    return interval;
  }

  public String getTicker() {
    return ticker;
  }
}
