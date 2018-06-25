package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
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
import org.apache.log4j.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import utils.CalcUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.limitSell;
import static java.lang.Math.max;

public class Wyatt {

	final static Logger logger = Logger.getLogger(Wyatt.class);
	private static Double percentageRatio = 1.0;
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

	public void printBalances() {
		Account account = client.getAccount();
		List<AssetBalance> balances = account.getBalances();

		for (AssetBalance balance : balances) {
			Double amount = Double.valueOf(balance.getFree()) + Double.valueOf(balance.getLocked());
			if (amount > 0.0) {
				logger.trace("Asset: " + balance.getAsset() + " - Balance: " + amount);
			}
		}
	}

	public void gatherMindData() {
		for (String ticker : tickers) {
			for (CandlestickInterval interval : intervalList) {
				gatherIntervalData(mindData, interval, ticker);
				new CalcUtils().sleeper(500);
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
		Double tierOne = 0.0;
		Double tierTwo = 0.0;
		Double tierThr = 0.0;
		Double tierFou = 0.0;
		Double tierFiv = 0.0;
		for (AverageData averageData : predictionData.averageData) {
			//total += (averageData.getCloseAvg()+averageData.getHighAvg())/2*percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 5)
				tierOne += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 25)
				tierTwo += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 100)
				tierThr += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 200)
				tierFou += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
			if (averageData.getNumberOfNodesAveraged() == 500)
				tierFiv += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
		}
		tierOne = Math.round(tierOne * 100.0) / 100.0;
		tierTwo = Math.round(tierTwo * 100.0) / 100.0;
		tierThr = Math.round(tierThr * 100.0) / 100.0;
		tierFou = Math.round(tierFou * 100.0) / 100.0;
		tierFiv = Math.round(tierFiv * 100.0) / 100.0;
		Double target = max(tierFiv, max(tierFou, max(tierThr, max(tierOne, tierTwo))));
		Double buyBack = Math.round(target * predictionData.buyBackAfterThisPercentage * 100.0) / 100.0;
		TickerStatistics lastPrice = null;
		for (HashMap.Entry<DataIdentifier, TickerStatistics> entry : mindData.getLastPriceData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				lastPrice = entry.getValue();
			}
		}
		Double lastPriceFloored = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
		Double sellConfidencePercentage = (lastPriceFloored / target * 100);
		int sellConfidence = (int)Math.floor(sellConfidencePercentage);
		logger.trace("Current: $" + lastPriceFloored + " Target: $" + target + " Buy back: $" + buyBack + " ::: Sell confidence: " + sellConfidence + "%");
		logger.trace("Tier 1(5): " + tierOne);
		logger.trace("Tier 2(25): " + tierTwo);
		logger.trace("Tier 3(100): " + tierThr);
		logger.trace("Tier 4(200): " + tierFou);
		logger.trace("Tier 5(500): " + tierFiv);
		//logger.trace("LEVEL ONE: Current: $" + lastPriceFloored + " Sell: $" + sellPrice + " Buy: $" + buyBackLevelOne);
		boolean trade = true;
		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		if (openOrders.size() > 0) {
			logger.trace("Orders for BTCUSDT are not empty, not trading for 120 seconds...");
			trade = false;
			new CalcUtils().sleeper(120000);
		}
		if ((lastPriceFloored > target) && trade) {
			//WE SHOULD SELL AND BUY!
			String message = "Deciding to sell! Current price: $" + lastPriceFloored + ". Buy back price: $" + buyBack;
			logger.info(message);
			//My bad I was sending a tweet
            if (message.length() < 280) {
                Twitter twitter = TwitterFactory.getSingleton();
                try {
                    Status status = twitter.updateStatus(message);
                    logger.trace("Sent tweet to @Wyatt__Dolores");
                } catch (TwitterException e) {
                    logger.error("ERROR SENDING TWEET: Reason: {}", e);
                }
            } else {
                logger.error("Could not send tweet, characters too long.");
            }
            performSellAndBuyBack(lastPriceFloored, buyBack);
		}
	}

	private void gatherIntervalData(MindData mindData, CandlestickInterval interval, String ticker) {
		List<Candlestick> candlesticks = new ArrayList<Candlestick>();
		try {
			candlesticks = client.getCandlestickBars(ticker, interval);
		} catch (Exception e) {
			logger.info("There was an exception while pulling interval data!");
			logger.trace("Interval: " + interval + " Ticker: " + ticker);
			logger.trace("Waiting for 120 seconds ...");
			new CalcUtils().sleeper(120000);
			logger.error("Error: ", e);
		}

		mindData.candlestickData.put(new DataIdentifier(interval, ticker), candlesticks);
		mindData.lastPriceData.put(new DataIdentifier(interval, ticker), client.get24HrPriceStatistics(ticker));
		mindData.candlestickIntAvgData.put(new DataIdentifier(interval, ticker),
				new CalcUtils().findAveragePrice(candlesticks));
	}

	public void performSellAndBuyBack(Double sellPrice, Double buyPrice) {
		Account account = client.getAccount();
		Double freeBTC = Double.valueOf(account.getAssetBalance("BTC").getFree());
		Double freeBTCFloored = Math.floor(Double.valueOf(freeBTC) * 10000.0) / 10000.0;
		logger.info("Amount of BTC to trade: " + freeBTCFloored);
		try {
			logger.info("Executing sell of: " + freeBTCFloored + " BTC @ $" + sellPrice);
			NewOrderResponse performSell = client.newOrder(
					limitSell("BTCUSDT", TimeInForce.GTC, freeBTCFloored.toString(), sellPrice.toString()));
			logger.info("Trade submitted: " + performSell.getTransactTime());
		} catch (Exception e) {
			logger.error("There was an exception thrown during the sell?: " + e.getMessage());
			e.printStackTrace();
		}
		new CalcUtils().sleeper(3000);
		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		logger.trace("Number of open BTCUSDT orders: " + openOrders.size());
		while (openOrders.size() > 0) {
			logger.trace("Orders for BTCUSDT are not empty, waiting 3 seconds...");
			new CalcUtils().sleeper(3000);
			openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		}
		new CalcUtils().sleeper(3000);
		account = client.getAccount();
		Double freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
		//Loop until above 10.0 USDT
		while (freeUSDT < 10.0) {
			logger.trace("Looping because we currently have less than 10 USDT. Waiting 15 seconds...");
			new CalcUtils().sleeper(15000);
			freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
		}
		Double freeUSDTFloored = Math.floor(Double.valueOf(freeUSDT) * 100.0) / 100.0;
		Double BTCtoBuy = freeUSDTFloored / buyPrice;
		Double BTCtoBuyFloored = Math.floor(Double.valueOf(BTCtoBuy) * 10000.0) / 10000.0;
		try {
			logger.info("Executing buy with: " + freeUSDTFloored + " USDT @ $" + buyPrice + " = " + BTCtoBuyFloored + " BTC");
			NewOrderResponse performBuy = client.newOrder(
					limitBuy("BTCUSDT", TimeInForce.GTC, BTCtoBuyFloored.toString(), buyPrice.toString()));
			logger.info("Trade submitted: " + performBuy.getTransactTime());
		} catch (Exception e) {
			logger.error("There was an exception thrown during the buy?: " + e.getMessage());
			e.printStackTrace();
		}
		new CalcUtils().sleeper(3000);
	}
}