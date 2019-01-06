package com.mtheory7.wyatt.utils;

import com.binance.api.client.domain.market.Candlestick;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class CalcUtils {
  public static double roundTo(double num, int places) {
    return Math.round(num * Math.pow(10, places)) / Math.pow(10, places);
  }

  public static double floorTo(double num, int places) {
    return Math.floor(num * Math.pow(10, places)) / Math.pow(10, places);
  }

  public static String getUpTimeString() {
    RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
    long seconds = rb.getUptime() / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    return days + "d " + hours % 24 + "h " + minutes % 60 + "m " + seconds % 60 + "s";
  }

  public Double findAveragePrice(List<Candlestick> candlesticks) {
    if (candlesticks.size() == 0) return 0.0;
    Double average = 0.0;
    for (Candlestick stick : candlesticks) {
      average += Double.valueOf(stick.getClose());
    }
    return average / candlesticks.size();
  }

  public void sleeper(int num) {
    try {
      Thread.sleep(num);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
