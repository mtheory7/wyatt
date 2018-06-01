package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import model.MindData;
import utils.CalcUtils;

public class Wyatt {

	private static int MAX_TRADES_PER_24HOURS = 10;
	private static String[] tickers = { "BTCUSDT", "ETHUSDT", "LTCUSDT" };
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
		for(String ticker : tickers)
		for(CandlestickInterval interval : CandlestickInterval.values()) {
			gatherIntervalData(dataToReturn, interval, ticker);
			new CalcUtils().sleeper(500);
			System.out.println("Fetched data for interval: " + interval.getIntervalId() + " ...");
		}

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

