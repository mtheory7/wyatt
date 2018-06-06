package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;
import model.DataIdentifier;
import model.data.AverageData;
import model.data.MindData;
import model.data.PredictionData;
import utils.CalcUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.limitSell;

public class Wyatt {

	private static Double percentageRatio = 1.0014;
	private static int MAX_TRADES_PER_24HOURS = 10;
	private static CandlestickInterval[] intervalList = {
			CandlestickInterval.ONE_MINUTE};
	private static String[] tickers = {"BTCUSDT"};
	public MindData mindData;
	public PredictionData predictionData;
	private BinanceApiClientFactory factory = null;
	private BinanceApiRestClient client = null;


	public Wyatt(String apiKey, String secret) {
		mindData = new MindData();
		predictionData = new PredictionData();
		factory = BinanceApiClientFactory.newInstance(apiKey, secret);
		client = factory.newRestClient();
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

	public void gatherMindData() {
		for (String ticker : tickers) {
			for (CandlestickInterval interval : intervalList) {
				gatherIntervalData(mindData, interval, ticker);
				new CalcUtils().sleeper(500);
				//System.out.println(ticker + " data fetched for interval: " + interval.getIntervalId() + " ...");
			}
		}
	}

	public void gatherPredictionData() {
		List<Candlestick> oneMinuteCandles = null;
		for (HashMap.Entry<DataIdentifier, List<Candlestick>> entry : mindData.getCandlestickData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				oneMinuteCandles = entry.getValue();
			}
		}
		List<List<Candlestick>> minuteData = new ArrayList<List<Candlestick>>();
		if (oneMinuteCandles != null) {
			int x = oneMinuteCandles.size() - 5;
			int y = oneMinuteCandles.size();
			minuteData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 25;
			minuteData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 100;
			minuteData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 200;
			minuteData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 500;
			minuteData.add(oneMinuteCandles.subList(x, y));
			minuteData.add(oneMinuteCandles.subList(0, y));
		}

		for (List<Candlestick> list : minuteData) {
			Double openAvg = 0.0;
			Double closeAvg = 0.0;
			Double lowAvg = 0.0;
			Double highAvg = 0.0;

			for (Candlestick stick : list) {
				openAvg += Double.valueOf(stick.getOpen());
				closeAvg += Double.valueOf(stick.getClose());
				lowAvg += Double.valueOf(stick.getLow());
				highAvg += Double.valueOf(stick.getHigh());
			}

			AverageData averageData = new AverageData();
			averageData.setOpenAvg(openAvg / list.size());
			averageData.setCloseAvg(closeAvg / list.size());
			averageData.setLowAvg(lowAvg / list.size());
			averageData.setHighAvg(highAvg / list.size());
			averageData.setNumberOfNodesAveraged(list.size());

			predictionData.averageData.add(averageData);
		}
		Double total = 0.0;

		for (AverageData averageData : predictionData.averageData) {
			//total += (averageData.getCloseAvg()+averageData.getHighAvg())/2*percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 5)
				total += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
		}

		total = Math.round(total * 100.0) / 100.0;
		Double buyBack = Math.round(total * predictionData.buyBackAfterThisPercentage * 100.0) / 100.0;
		//total = total/predictionData.averageData.size();
		System.out.println("Target sell price: $" + total + " ::: Buy back at: $" + buyBack);

		TickerStatistics lastPrice = null;

		for (HashMap.Entry<DataIdentifier, TickerStatistics> entry : mindData.getLastPriceData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				lastPrice = entry.getValue();
			}
		}
		if (Double.valueOf(lastPrice.getLastPrice()) > total) {
			Double z = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
			//WE SHOULD SELL AND BUY!
			System.out.println("\nDeciding to sell! Target price was: $" + total + ". Current price was: $" + z + ". Buy back price is: " + buyBack + "\n");
			performSellAndBuyBack(z, buyBack);
		}
	}

	private void gatherIntervalData(MindData mindData, CandlestickInterval interval, String ticker) {
		List<Candlestick> candlesticks = client.getCandlestickBars(ticker, interval);
		mindData.candlestickData.put(new DataIdentifier(interval, ticker), candlesticks);
		mindData.lastPriceData.put(new DataIdentifier(interval, ticker), client.get24HrPriceStatistics(ticker));
		mindData.candlestickIntAvgData.put(new DataIdentifier(interval, ticker),
				new CalcUtils().findAveragePrice(candlesticks));
	}

	public void performSellAndBuyBack(Double sellPrice, Double buyPrice) {
		Account account = client.getAccount();
		Double freeBTC = Double.valueOf(account.getAssetBalance("BTC").getFree());
		Double freeBTCRounded = Math.round(Double.valueOf(freeBTC) * 10000.0) / 10000.0;
		System.out.println("Amount of BTC to trade: " + freeBTCRounded);

		try {
			NewOrderResponse performSell = client.newOrder(
					limitSell("BTCUSDT", TimeInForce.GTC, freeBTCRounded.toString(), sellPrice.toString()));
			System.out.println("Trade submitted: " + performSell.getTransactTime());
		} catch (Exception e) {
			System.out.println("There was an exception thrown during the sell?: " + e.getMessage());
		}

		new CalcUtils().sleeper(1500);

		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		while (openOrders.size() > 0) {
			new CalcUtils().sleeper(3000);
			openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		}

		try {
			NewOrderResponse performBuy = client.newOrder(
					limitBuy("BTCUSDT", TimeInForce.GTC, freeBTCRounded.toString(), buyPrice.toString()));
			System.out.println("Trade submitted: " + performBuy.getTransactTime());
		} catch (Exception e) {
			System.out.println("There was an exception thrown during the buy?: " + e.getMessage());
		}

		new CalcUtils().sleeper(1500);

	}
}

