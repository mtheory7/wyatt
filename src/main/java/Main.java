import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import mind.Wyatt;
import utils.CalcUtils;

import java.util.List;

public class Main {
  public static void main(String[] args) {

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
            "VENnsLCOxIf3TxzMrEHf4zgogZzL4lz7igd3DiJExM1X25V364t6cxjiLeTCrj9q",
            "AiDrDX35H6yeA1WkPHj9YW9GSsRZ0iAEX6rEAwxleGjQpkDQSj7iO3kX4wqC83oE");
    BinanceApiRestClient client = factory.newRestClient();
    //TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");

    Wyatt.playSweetWater();

    List<Candlestick> candles1minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE);
    List<Candlestick> candles3minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.THREE_MINUTES);
    List<Candlestick> candles5minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIVE_MINUTES);
    List<Candlestick> candles15minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIFTEEN_MINUTES);
    List<Candlestick> candles30minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.HALF_HOURLY);
    List<Candlestick> candles1hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY);
    List<Candlestick> candles2hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.TWO_HOURLY);
    List<Candlestick> candles4hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FOUR_HOURLY);
    List<Candlestick> candles8hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.EIGHT_HOURLY);

    Double currentPrice = Double.valueOf(client.get24HrPriceStatistics("BTCUSDT").getLastPrice());

    Double candles1minuteAvg = new CalcUtils().findAveragePrice(candles1minute);
    Double candles3minuteAvg = new CalcUtils().findAveragePrice(candles3minute);
    Double candles5minuteAvg = new CalcUtils().findAveragePrice(candles5minute);
    Double candles15minuteAvg = new CalcUtils().findAveragePrice(candles15minute);
    Double candles30minuteAvg = new CalcUtils().findAveragePrice(candles30minute);
    Double candles1hourAvg = new CalcUtils().findAveragePrice(candles1hour);
    Double candles2hourAvg = new CalcUtils().findAveragePrice(candles2hour);
    Double candles4hourAvg = new CalcUtils().findAveragePrice(candles4hour);
    Double candles8hourAvg = new CalcUtils().findAveragePrice(candles8hour);

    System.out.println("Time interval: 1 minute   =====  Number of candlesticks fetched: " + candles1minute.size() + " :: Average: $" + candles1minuteAvg);
    System.out.println("Time interval: 3 minutes  =====  Number of candlesticks fetched: " + candles3minute.size() + " :: Average: $" + candles3minuteAvg);
    System.out.println("Time interval: 5 minutes  =====  Number of candlesticks fetched: " + candles5minute.size() + " :: Average: $" + candles5minuteAvg);
    System.out.println("Time interval: 15 minutes =====  Number of candlesticks fetched: " + candles15minute.size() + " :: Average: $" + candles15minuteAvg);
    System.out.println("Time interval: 1/2 hour   =====  Number of candlesticks fetched: " + candles30minute.size() + " :: Average: $" + candles30minuteAvg);
    System.out.println("Time interval: 1 hour     =====  Number of candlesticks fetched: " + candles1hour.size() + " :: Average: $" + candles1hourAvg);
    System.out.println("Time interval: 2 hours    =====  Number of candlesticks fetched: " + candles2hour.size() + " :: Average: $" + candles2hourAvg);
    System.out.println("Time interval: 4 hours    =====  Number of candlesticks fetched: " + candles4hour.size() + " :: Average: $" + candles4hourAvg);
    System.out.println("Time interval: 8 hours    =====  Number of candlesticks fetched: " + candles8hour.size() + " :: Average: $" + candles8hourAvg);
    System.out.println("\nCURRENT PRICE === $" + currentPrice);
  }
}

/*String printString = tickerStatistics.getLastPrice() + "\n";

Account account = client.getAccount();
for (AssetBalance balance : account.getBalances()) {
  Double assetBalance = Double.parseDouble(balance.getLocked()) + Double.parseDouble(balance.getFree());
  if (assetBalance != 0.0)
    printString += (balance.getAsset() + " " + assetBalance +  "\n");
}*/
