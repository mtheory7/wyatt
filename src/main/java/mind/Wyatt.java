package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import model.MindData;
import utils.CalcUtils;

public class Wyatt {
  public static void playSweetWater() {
    System.out.println("\nLOADING SYSTEM ... \n");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println(" .----------------. .----------------. .----------------. .----------------. .----------------. ");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| .--------------. | .--------------. | .--------------. | .--------------. | .--------------. |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| | _____  _____ | | |  ____  ____  | | |      __      | | |  _________   | | |  _________   | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| ||_   _||_   _|| | | |_  _||_  _| | | |     /  \\     | | | |  _   _  |  | | | |  _   _  |  | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| |  | | /\\ | |  | | |   \\ \\  / /   | | |    / /\\ \\    | | | |_/ | | \\_|  | | | |_/ | | \\_|  | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| |  | |/  \\| |  | | |    \\ \\/ /    | | |   / ____ \\   | | |     | |      | | |     | |      | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| |  |   /\\   |  | | |    _|  |_    | | | _/ /    \\ \\_ | | |    _| |_     | | |    _| |_     | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| |  |__/  \\__|  | | |   |______|   | | ||____|  |____|| | |   |_____|    | | |   |_____|    | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| |              | | |              | | |              | | |              | | |              | |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println("| '--------------' | '--------------' | '--------------' | '--------------' | '--------------' |");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
    System.out.println(" '----------------' '----------------' '----------------' '----------------' '----------------' ");
    new CalcUtils().sleeper(CalcUtils.SLEEP_NUM);
  }

  public MindData gatherData() {

    MindData dataToReturn = new MindData();

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
            "VENnsLCOxIf3TxzMrEHf4zgogZzL4lz7igd3DiJExM1X25V364t6cxjiLeTCrj9q",
            "AiDrDX35H6yeA1WkPHj9YW9GSsRZ0iAEX6rEAwxleGjQpkDQSj7iO3kX4wqC83oE");
    BinanceApiRestClient client = factory.newRestClient();
    //TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");

    dataToReturn.candlestickData.put(CandlestickInterval.ONE_MINUTE, client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE));
    Double currentPrice = Double.valueOf(client.get24HrPriceStatistics("BTCUSDT").getLastPrice());
    Double candles1minuteAvg = new CalcUtils().findAveragePrice(dataToReturn.candlestickData.get(CandlestickInterval.ONE_MINUTE));

    return dataToReturn;
  }
}

    /*List<Candlestick> candles3minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.THREE_MINUTES);
    List<Candlestick> candles5minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIVE_MINUTES);
    List<Candlestick> candles15minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FIFTEEN_MINUTES);
    List<Candlestick> candles30minute = client.getCandlestickBars("BTCUSDT", CandlestickInterval.HALF_HOURLY);
    List<Candlestick> candles1hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY);
    List<Candlestick> candles2hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.TWO_HOURLY);
    List<Candlestick> candles4hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.FOUR_HOURLY);
    List<Candlestick> candles8hour = client.getCandlestickBars("BTCUSDT", CandlestickInterval.EIGHT_HOURLY);*/
    /*Double candles3minuteAvg = new CalcUtils().findAveragePrice(candles3minute);
    Double candles5minuteAvg = new CalcUtils().findAveragePrice(candles5minute);
    Double candles15minuteAvg = new CalcUtils().findAveragePrice(candles15minute);
    Double candles30minuteAvg = new CalcUtils().findAveragePrice(candles30minute);
    Double candles1hourAvg = new CalcUtils().findAveragePrice(candles1hour);
    Double candles2hourAvg = new CalcUtils().findAveragePrice(candles2hour);
    Double candles4hourAvg = new CalcUtils().findAveragePrice(candles4hour);
    Double candles8hourAvg = new CalcUtils().findAveragePrice(candles8hour);*/
    /*String printString = tickerStatistics.getLastPrice() + "\n";
    Account account = client.getAccount();
    for (AssetBalance balance : account.getBalances()) {
      Double assetBalance = Double.parseDouble(balance.getLocked()) + Double.parseDouble(balance.getFree());
      if (assetBalance != 0.0)
        printString += (balance.getAsset() + " " + assetBalance +  "\n");
    }*/