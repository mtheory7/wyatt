package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import model.MindData;
import utils.CalcUtils;

public class Wyatt {

    private MindData dataToReturn;

    public Wyatt() {
        dataToReturn = new MindData();
    }

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

        gatherIntervalData(dataToReturn, CandlestickInterval.ONE_MINUTE, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.THREE_MINUTES, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.FIVE_MINUTES, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.FIFTEEN_MINUTES, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.HALF_HOURLY, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.HOURLY, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.TWO_HOURLY, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.FOUR_HOURLY, "BTCUSDT");
        gatherIntervalData(dataToReturn, CandlestickInterval.EIGHT_HOURLY, "BTCUSDT");

        return dataToReturn;
    }

    private void gatherIntervalData(MindData mindData, CandlestickInterval interval, String ticker) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
                "VENnsLCOxIf3TxzMrEHf4zgogZzL4lz7igd3DiJExM1X25V364t6cxjiLeTCrj9q",
                "AiDrDX35H6yeA1WkPHj9YW9GSsRZ0iAEX6rEAwxleGjQpkDQSj7iO3kX4wqC83oE");
        BinanceApiRestClient client = factory.newRestClient();

        mindData.candlestickData.put(interval, client.getCandlestickBars(ticker, interval));
        mindData.lastPriceData.put(interval, client.get24HrPriceStatistics(ticker));
        mindData.candlestickIntAvgData.put(interval,
                new CalcUtils().findAveragePrice(mindData.candlestickData.get(interval)));
    }
}

