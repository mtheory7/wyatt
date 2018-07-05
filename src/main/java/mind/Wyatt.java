package mind;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;
import model.DataIdentifier;
import model.data.AverageData;
import model.data.MindData;
import model.data.PredictionData;
import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import utils.CalcUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.limitSell;
import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static java.lang.Math.max;

public class Wyatt {
	private final static Logger logger = Logger.getLogger(Wyatt.class);
	private static CandlestickInterval[] intervalList = {CandlestickInterval.ONE_MINUTE, CandlestickInterval.THREE_MINUTES};
	private static String[] tickers = {"BTCUSDT"};
	private MindData mindData;
	private PredictionData predictionData;
	private BinanceApiRestClient client;
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;

	/**
	 * Instantiates a new instance of Wyatt's mind. It requires an API Key
	 * and the API Key secret to pull the latest trading data, and to
	 * execute trades.
	 *
	 * @param binanceAPIKey    The Binance API Key
	 * @param binanceAPISecret The secret for the Binance API Key
	 */
	public Wyatt(String binanceAPIKey, String binanceAPISecret) {
		mindData = new MindData();
		predictionData = new PredictionData();
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(binanceAPIKey, binanceAPISecret);
		client = factory.newRestClient();
	}

	/**
	 * Sets the credentials that are needed for tweeting alerts when
	 * Wyatt decides to sell and buy back.
	 *
	 * @param consumerKey       Twitter Consumer Key
	 * @param consumerSecret    Twitter Consumer Secret
	 * @param accessToken       Twitter Access Token
	 * @param accessTokenSecret Twitter Access Token Secret
	 */
	public void setTwitterCreds(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
	}

	/**
	 * Logs the latest balances of the Binance account. This is useful
	 * when diagnosing trading patterns and trade logic.
	 */
	public void printBalances() {
		Account account = client.getAccount();
		//Pull the latest account balance info from Binance
		List<AssetBalance> balances = account.getBalances();
		for (AssetBalance balance : balances) {
			//Combine the amount of idle assets, and the amount in trade currently
			Double amount = Double.valueOf(balance.getFree()) + Double.valueOf(balance.getLocked());
			if (amount > 0.0) {
				logger.trace("Asset: " + balance.getAsset() + " - Balance: " + amount);
			}
		}
	}

	/**
	 * Retrieves data from the ticker data pulled from Binance. This data
	 * is then used later for predicting a selling price.
	 */
	public void gatherMindData() {
		for (String ticker : tickers) {
			for (CandlestickInterval interval : intervalList) {
				gatherIntervalData(mindData, interval, ticker);
				new CalcUtils().sleeper(500);
			}
		}
	}

	/**
	 * Use the gathered data to attempt to predict a price to sell at, and
	 * then a price to buy back at. When the price exceeds that target value,
	 * perform a sell and a buy back to make an incremental amount of money.
	 */
	public void predictAndTrade() {
		List<Candlestick> oneMinuteCandles = null;
		List<Candlestick> thrMinuteCandles = null;
		for (HashMap.Entry<DataIdentifier, List<Candlestick>> entry : mindData.getCandlestickData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				oneMinuteCandles = entry.getValue();
			}
			if (entry.getKey().getInterval() == CandlestickInterval.THREE_MINUTES
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				thrMinuteCandles = entry.getValue();
			}
		}
		List<List<Candlestick>> candleData = new ArrayList<List<Candlestick>>();
		//Separate out some lists of varying lengths of candles
		if (oneMinuteCandles != null) {
			int x = oneMinuteCandles.size() - 5;
			int y = oneMinuteCandles.size();
			candleData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 25;
			candleData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 100;
			candleData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 200;
			candleData.add(oneMinuteCandles.subList(x, y));
			x = oneMinuteCandles.size() - 500;
			candleData.add(oneMinuteCandles.subList(x, y));
			candleData.add(oneMinuteCandles.subList(0, y));
		}
		//Now use the candle lists to calculate average data used for price prediction
		for (List<Candlestick> list : candleData) {
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
		//Calculate 6th tier
		Double openAvg = 0.0;
		Double closeAvg = 0.0;
		Double lowAvg = 0.0;
		Double highAvg = 0.0;
		for (Candlestick stick : thrMinuteCandles) {
			openAvg += Double.valueOf(stick.getOpen());
			closeAvg += Double.valueOf(stick.getClose());
			lowAvg += Double.valueOf(stick.getLow());
			highAvg += Double.valueOf(stick.getHigh());
		}
		AverageData avgData = new AverageData();
		avgData.setOpenAvg(openAvg / thrMinuteCandles.size());
		avgData.setCloseAvg(closeAvg / thrMinuteCandles.size());
		avgData.setLowAvg(lowAvg / thrMinuteCandles.size());
		avgData.setHighAvg(highAvg / thrMinuteCandles.size());
		avgData.setNumberOfNodesAveraged(thrMinuteCandles.size());
		predictionData.averageData.add(avgData);
		//Set percentage
		Double TARGET_PERCENT_RATIO = 1.0004;
		Double tierOne = 0.0;
		Double tierTwo = 0.0;
		Double tierThr = 0.0;
		Double tierFou = 0.0;
		Double tierFiv = 0.0;
		//Calculate averages, and use those and a ratio to create tiered target prices
		for (AverageData averageData : predictionData.averageData) {

			if (averageData.getNumberOfNodesAveraged() == 5)
				tierOne += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
			if (averageData.getNumberOfNodesAveraged() == 25)
				tierTwo += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
			if (averageData.getNumberOfNodesAveraged() == 100)
				tierThr += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
			if (averageData.getNumberOfNodesAveraged() == 200)
				tierFou += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
			if (averageData.getNumberOfNodesAveraged() == 500)
				tierFiv += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
		}
		Double tierSix = (avgData.getCloseAvg() + avgData.getHighAvg()) / 2 * TARGET_PERCENT_RATIO;
		//Round those target prices
		tierOne = Math.round(tierOne * 100.0) / 100.0;
		tierTwo = Math.round(tierTwo * 100.0) / 100.0;
		tierThr = Math.round(tierThr * 100.0) / 100.0;
		tierFou = Math.round(tierFou * 100.0) / 100.0;
		tierFiv = Math.round(tierFiv * 100.0) / 100.0;
		tierSix = Math.round(tierSix * 100.0) / 100.0;
		//Find max of all the tiered target prices
		Double target = max(tierSix, max(tierFiv, max(tierFou, max(tierThr, max(tierOne, tierTwo)))));
		//Calculate the buy back price using a configurable buy back percentage ratio
		Double buyBack = Math.round(target * PredictionData.buyBackAfterThisPercentage * 100.0) / 100.0;
		TickerStatistics lastPrice = null;
		//Pull the latest value from mindData
		for (HashMap.Entry<DataIdentifier, TickerStatistics> entry : mindData.getLastPriceData().entrySet()) {
			if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
					&& entry.getKey().getTicker().equals("BTCUSDT")) {
				lastPrice = entry.getValue();
			}
		}
		Double lastPriceFloored = 0.0;
		if (lastPrice != null && lastPrice.getLastPrice() != null) {
			//Round the last price that was just pulled
			lastPriceFloored = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
		}
		//Calculate and round sell confidence percentage
		Double sellConfidence = Math.round((lastPriceFloored / target * 100) * 1000.0) / 1000.0;
		logger.trace("Current: $" + lastPriceFloored + " Target: $" + target + " Buy back: $" + buyBack + " ::: " + sellConfidence + "%");
		logger.trace("Tier_1: " + tierOne + " Tier_2: " + tierTwo + " Tier_3: " + tierThr + " Tier_4: " + tierFou + " Tier_5: " + tierFiv + " Tier_6: " + tierSix);
		boolean trade = true;
		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		if (openOrders.size() > 0) {
			logger.trace("Number of open BTCUSDT orders: " + openOrders.size());
			trade = false;
			//See how far we are from the open order
			Order openOrder = openOrders.get(0);
			if (openOrder != null) {
				Double currentMargin = lastPriceFloored / Double.valueOf(openOrder.getPrice());
				Double currentMarginPercent = (currentMargin - 1) * 100;
				currentMarginPercent = Math.round(currentMarginPercent * 100.0) / 100.0;
				logger.trace("Current buy back margin percentage: " + currentMarginPercent + "%");
				if (currentMarginPercent > 10) {
					logger.trace("Executing market buy back at $" + lastPriceFloored);
					executeMarketBuyBack();
				} else {
					logger.trace("Orders for BTCUSDT are not empty, not trading for 120 seconds...");
					new CalcUtils().sleeper(120000);
				}
			}
		}
		if ((lastPriceFloored > target) && trade) {
			//WE SHOULD SELL AND BUY!
			String message = "Deciding to sell! Current: $" + lastPriceFloored + " Target: $" + target + " Buy back: $" + buyBack;
			logger.info(message);
			sendTweet(message);
			//My bad I was sending a tweet
			performSellAndBuyBack(lastPriceFloored, buyBack);
		}
	}

	/**
	 * Function to connect to Binance, and pull the information in candle form
	 * used to run the bot.
	 *
	 * @param mindData The structure to save the data to
	 * @param interval The interval to grab candle data for
	 * @param ticker   The ticker to grab candle data for
	 */
	private void gatherIntervalData(MindData mindData, CandlestickInterval interval, String ticker) {
		List<Candlestick> candlesticks = new ArrayList<Candlestick>();
		try {
			//Make the GET call to Binance
			candlesticks = client.getCandlestickBars(ticker, interval);
		} catch (Exception e) {
			logger.info("There was an exception while pulling interval data!");
			logger.trace("Interval: " + interval + " Ticker: " + ticker);
			logger.trace("Waiting for 120 seconds ...");
			new CalcUtils().sleeper(120000);
			logger.error("Error: ", e);
		}
		//Save the pulled data to the passed in data structure
		mindData.candlestickData.put(new DataIdentifier(interval, ticker), candlesticks);
		mindData.lastPriceData.put(new DataIdentifier(interval, ticker), client.get24HrPriceStatistics(ticker));
		mindData.candlestickIntAvgData.put(new DataIdentifier(interval, ticker),
				new CalcUtils().findAveragePrice(candlesticks));
	}

	/**
	 * Perform a sell and buy at the passed in values. Uses the Binance
	 * configuration to execute these trades.
	 *
	 * @param sellPrice Price to sell at
	 * @param buyPrice  Price to buy at
	 */
	private void performSellAndBuyBack(Double sellPrice, Double buyPrice) {
		Account account = client.getAccount();
		//Find out how much free asset there is to trade
		Double freeBTC = Double.valueOf(account.getAssetBalance("BTC").getFree());
		Double freeBTCFloored = Math.floor(Double.valueOf(freeBTC) * 10000.0) / 10000.0;
		logger.info("Amount of BTC to trade: " + freeBTCFloored);
		try {
			logger.info("Executing sell of: " + freeBTCFloored + " BTC @ $" + sellPrice);
			//Submit the binance sell
			NewOrderResponse performSell = client.newOrder(
					limitSell("BTCUSDT", TimeInForce.GTC, freeBTCFloored.toString(), sellPrice.toString()));
			logger.info("Trade submitted: " + performSell.getTransactTime());
		} catch (Exception e) {
			logger.error("There was an exception thrown during the sell?: " + e.getMessage());
			e.printStackTrace();
		}
		new CalcUtils().sleeper(3000);
		//Wait and make sure that the trade executed. If not, keep waiting
		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		logger.trace("Number of open BTCUSDT orders: " + openOrders.size());
		while (openOrders.size() > 0) {
			logger.trace("Orders for BTCUSDT are not empty, waiting 3 seconds...");
			new CalcUtils().sleeper(3000);
			openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		}
		new CalcUtils().sleeper(3000);
		account = client.getAccount();
		//Verify that we have the correct amount of asset to trade
		Double freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
		//Loop until above 10.0 USDT
		while (freeUSDT < 10.0) {
			logger.trace("Looping because we currently have less than 10 USDT. Waiting 15 seconds...");
			new CalcUtils().sleeper(15000);
			freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
		}
		//Calculate and round the values in preparation for buying back
		Double freeUSDTFloored = Math.floor(freeUSDT * 100.0) / 100.0;
		Double BTCtoBuy = freeUSDTFloored / buyPrice;
		Double BTCtoBuyFloored = Math.floor(BTCtoBuy * 10000.0) / 10000.0;
		try {
			logger.info("Executing buy with: " + freeUSDTFloored + " USDT @ $" + buyPrice + " = " + BTCtoBuyFloored + " BTC");
			//Submit the Binance buy back
			NewOrderResponse performBuy = client.newOrder(
					limitBuy("BTCUSDT", TimeInForce.GTC, BTCtoBuyFloored.toString(), buyPrice.toString()));
			logger.info("Trade submitted: " + performBuy.getTransactTime());
		} catch (Exception e) {
			logger.error("There was an exception thrown during the buy?: " + e.getMessage());
			e.printStackTrace();
		}
		new CalcUtils().sleeper(3000);
	}

	/**
	 * Execute a market buy back
	 */
	private void executeMarketBuyBack() {
		//Cancel all open orders
		List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
		for (Order order : openOrders) {
			logger.trace("Cancelling order: " + order.getOrderId());
			client.cancelOrder(new CancelOrderRequest("BTCUSDT", order.getOrderId()));
		}
		//Execute market buy back
		new CalcUtils().sleeper(3000);
		Account account = client.getAccount();
		//Find out how much free asset there is to trade
		Double freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
		Double freeUSDTFloored = Math.floor(freeUSDT * 100.0) / 100.0;
		String message = "Executing market buy back with " + freeUSDTFloored + " USDT";
		logger.trace(message);
		sendTweet(message);
		NewOrderResponse newOrderResponse = client.newOrder(marketBuy("BTCUSDT", freeUSDTFloored.toString()));
		new CalcUtils().sleeper(15000);
	}

	/**
	 * Function to send a tweet. Pass in the message to send and it will use
	 * the preconfigured Twitter OAuth credentials.
	 *
	 * @param message The message to tweet
	 */
	private void sendTweet(String message) {
		//Use OAuth to pass Twitter credentials
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		//Tweets can only be 280 characters long error if longer
		if (message.length() <= 280) {
			try {
				twitter.updateStatus(message);
				logger.trace("Sent tweet to @WestworldWyatt");
			} catch (TwitterException e) {
				logger.error("ERROR SENDING TWEET: Reason: {}", e);
			}
		} else {
			logger.error("Tweet too long!! (That's what she said)");
		}
	}
}