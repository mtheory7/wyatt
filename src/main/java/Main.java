import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;
import utils.CalcUtils;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
            "VENnsLCOxIf3TxzMrEHf4zgogZzL4lz7igd3DiJExM1X25V364t6cxjiLeTCrj9q",
            "AiDrDX35H6yeA1WkPHj9YW9GSsRZ0iAEX6rEAwxleGjQpkDQSj7iO3kX4wqC83oE");
    BinanceApiRestClient client = factory.newRestClient();

    TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");
    List<Candlestick> candles1minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE);
    List<Candlestick> candles5minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIVE_MINUTES);
    List<Candlestick> candles15minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIFTEEN_MINUTES);
    List<Candlestick> candles30minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.HALF_HOURLY);
    Double currentPrice = Double.valueOf(client.get24HrPriceStatistics("BTCUSDT").getLastPrice());
    Double candles1minuteAvg = new CalcUtils().findAveragePrice(candles1minute);
    Double candles5minuteAvg = new CalcUtils().findAveragePrice(candles5minute);
    Double candles15minuteAvg = new CalcUtils().findAveragePrice(candles15minute);
    Double candles30minuteAvg = new CalcUtils().findAveragePrice(candles30minute);
    System.out.println();
  }
}

/*String printString = tickerStatistics.getLastPrice() + "\n";

Account account = client.getAccount();
for (AssetBalance balance : account.getBalances()) {
  Double assetBalance = Double.parseDouble(balance.getLocked()) + Double.parseDouble(balance.getFree());
  if (assetBalance != 0.0)
    printString += (balance.getAsset() + " " + assetBalance +  "\n");
}*/