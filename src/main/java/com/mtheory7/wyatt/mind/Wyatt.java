package com.mtheory7.wyatt.mind;

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
import com.mtheory7.wyatt.model.DataIdentifier;
import com.mtheory7.wyatt.model.data.MindData;
import com.mtheory7.wyatt.model.data.PredictionEngine;
import com.mtheory7.wyatt.utils.CalcUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.binance.api.client.domain.account.NewOrder.*;

@Service
public class Wyatt {
  private static final boolean DEVELOPING = true;
  private static final Logger logger = Logger.getLogger(Wyatt.class);
  private static CandlestickInterval[] intervalList = {
          CandlestickInterval.ONE_MINUTE, CandlestickInterval.THREE_MINUTES,
          CandlestickInterval.FIVE_MINUTES, CandlestickInterval.FIFTEEN_MINUTES
  };
  private static String[] tickers = {"BTCUSDT"};
  private MindData mindData;
  private PredictionEngine predictionEngine;
  private BinanceApiRestClient client;
  private String consumerKey;
  private String consumerSecret;
  private String accessToken;
  private String accessTokenSecret;

  public Wyatt() {
  }

  /**
   * Sets the credentials that are needed for interacting with Binance
   *
   * @param binanceAPIKey    Binance API Key
   * @param binanceAPISecret Binance API Secret
   */
  public void setBinanceCreds(String binanceAPIKey, String binanceAPISecret) {
    mindData = new MindData();
    predictionEngine = new PredictionEngine();
    BinanceApiClientFactory factory =
            BinanceApiClientFactory.newInstance(binanceAPIKey, binanceAPISecret);
    client = factory.newRestClient();
  }

  /**
   * Sets the credentials that are needed for tweeting alerts when Wyatt decides to sell and buy
   * back.
   *
   * @param consumerKey       Twitter Consumer Key
   * @param consumerSecret    Twitter Consumer Secret
   * @param accessToken       Twitter Access Token
   * @param accessTokenSecret Twitter Access Token Secret
   */
  public void setTwitterCreds(
          String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.accessToken = accessToken;
    this.accessTokenSecret = accessTokenSecret;
  }

  public String getTotalBalance() {
    Account account = client.getAccount();
    // Pull the latest account balance info from Binance
    List<AssetBalance> balances = account.getBalances();
    Double estimatedBalance = 0.0;
    for (AssetBalance balance : balances) {
      Double amount = Double.valueOf(balance.getFree()) + Double.valueOf(balance.getLocked());
      if (amount > 0.0) {
        if (balance.getAsset().equals("BTC")) {
          estimatedBalance += amount;
        } else {
          estimatedBalance += valueInBTC(amount, balance.getAsset());
        }
      }
    }
    estimatedBalance = Math.round(estimatedBalance * 100000000.0) / 100000000.0;
    return estimatedBalance.toString();
  }

  /**
   * Logs the latest balances of the Binance account. This is useful when diagnosing trading
   * patterns and trade logic.
   */
  public void printBalances() {
    Account account = client.getAccount();
    // Pull the latest account balance info from Binance
    List<AssetBalance> balances = account.getBalances();
    Double estimatedBalance = 0.0;
    for (AssetBalance balance : balances) {
      // Combine the amount of idle assets, and the amount in trade currently
      Double amount = Double.valueOf(balance.getFree()) + Double.valueOf(balance.getLocked());
      if (amount > 0.0) {
        logger.trace("Asset: " + balance.getAsset() + " - Balance: " + amount);
        if (balance.getAsset().equals("BTC")) {
          estimatedBalance += amount;
        } else {
          estimatedBalance += valueInBTC(amount, balance.getAsset());
        }
      }
    }
    estimatedBalance = Math.round(estimatedBalance * 100000000.0) / 100000000.0;
    Double percentOnInvenstment = ((estimatedBalance / 0.007) * 100) - 100;
    percentOnInvenstment = Math.round(percentOnInvenstment * 100.0) / 100.0;
    logger.trace("Estimated total account value: " + estimatedBalance + " BTC");
    logger.trace("Profit since starting (0.007 BTC): " + percentOnInvenstment + "%");
  }

  /**
   * Estimate the value of a given amount/ticker in BTC
   *
   * @param amount The amount of an asset
   * @param ticker The ticker of the asset to estimate
   */
  private Double valueInBTC(Double amount, String ticker) {
    if (ticker.equals("USDT")) {
      TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");
      return amount / Double.valueOf(tickerStatistics.getLastPrice());
    } else {
      ticker = ticker + "BTC";
      TickerStatistics tickerStatistics = client.get24HrPriceStatistics(ticker);
      return Double.valueOf(tickerStatistics.getLastPrice()) * amount;
    }
  }

  /**
   * Retrieves data from the ticker data pulled from Binance. This data is then used later for
   * predicting a selling price.
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
   * Use the gathered data to attempt to predict a price to sell at, and then a price to buy back
   * at. When the price exceeds that target value, perform a sell and a buy back to make an
   * incremental amount of money.
   */
  public void predictAndTrade() {
    if (DEVELOPING) {
      reportDevMode();
    }
    predictionEngine.executeThoughtProcess(mindData);
    Double target = predictionEngine.targetPrice;
    Double buyBack =
            Math.round(target * PredictionEngine.buyBackAfterThisPercentage * 100.0) / 100.0;
    TickerStatistics lastPrice = null;
    for (HashMap.Entry<DataIdentifier, TickerStatistics> entry :
            mindData.getLastPriceData().entrySet()) {
      if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
              && entry.getKey().getTicker().equals("BTCUSDT")) {
        lastPrice = entry.getValue();
      }
    }
    Double lastPriceFloored = 0.0;
    if (lastPrice != null && lastPrice.getLastPrice() != null) {
      lastPriceFloored = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
    }
    Double sellConfidence = Math.round((lastPriceFloored / target * 100) * 1000.0) / 1000.0;
    logger.trace(
            "Current: $" + lastPriceFloored + " Target: $" + target + " Buy back: $" + buyBack);
    logger.trace("Sell confidence: " + sellConfidence + "%");
    boolean trade = true;
    List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
    if (openOrders.size() > 0) {
      logger.trace("Number of open BTCUSDT orders: " + openOrders.size());
      trade = false;
      Order openOrder = openOrders.get(0);
      if (openOrder != null) {
        Double currentMargin = lastPriceFloored / Double.valueOf(openOrder.getPrice());
        Double currentMarginPercent = (currentMargin - 1) * 100;
        currentMarginPercent = Math.round(currentMarginPercent * 100.0) / 100.0;
        Double buyBackDifference = (lastPriceFloored - Double.valueOf(openOrder.getPrice()));
        buyBackDifference = Math.round(buyBackDifference * 100.0) / 100.0;
        logger.trace(
                "Current buy back: " + currentMarginPercent + "% ($" + buyBackDifference + ")");
        if (currentMarginPercent > 7.5) {
          logger.trace("Deciding to submit a market buy back at $" + lastPriceFloored);
          if (!DEVELOPING) {
            executeMarketBuyBack();
          } else {
            reportDevMode();
          }
        } else {
          logger.trace("Orders for BTCUSDT are not empty, not trading for 120 seconds...");
          new CalcUtils().sleeper(120000);
        }
      }
    }
    if ((lastPriceFloored > target) && trade) {
      String message =
              "Deciding to sell! Current: $"
                      + lastPriceFloored
                      + " Target: $"
                      + target
                      + " Buy back: $"
                      + buyBack;
      logger.info(message);
      if (!DEVELOPING) {
        performSellAndBuyBack(lastPriceFloored, buyBack, message);
      } else {
        reportDevMode();
      }
    }
  }

  /**
   * Function to connect to Binance, and pull the information in candle form used to run the bot.
   *
   * @param mindData The structure to save the data to
   * @param interval The interval to grab candle data for
   * @param ticker   The ticker to grab candle data for
   */
  private void gatherIntervalData(MindData mindData, CandlestickInterval interval, String ticker) {
    List<Candlestick> candlesticks = new ArrayList<Candlestick>();
    try {
      // Make the GET call to Binance
      candlesticks = client.getCandlestickBars(ticker, interval);
    } catch (Exception e) {
      logger.info("There was an exception while pulling interval data!");
      logger.trace("Interval: " + interval + " Ticker: " + ticker);
      logger.trace("Waiting for 120 seconds ...");
      new CalcUtils().sleeper(120000);
      logger.error("Error: ", e);
    }
    // Save the pulled data to the passed in data structure
    mindData.candlestickData.put(new DataIdentifier(interval, ticker), candlesticks);
    mindData.lastPriceData.put(
            new DataIdentifier(interval, ticker), client.get24HrPriceStatistics(ticker));
    mindData.candlestickIntAvgData.put(
            new DataIdentifier(interval, ticker), new CalcUtils().findAveragePrice(candlesticks));
  }

  /**
   * Perform a sell and buy at the passed in values. Uses the Binance configuration to execute these
   * trades.
   *
   * @param sellPrice Price to sell at
   * @param buyPrice  Price to buy at
   */
  private void performSellAndBuyBack(Double sellPrice, Double buyPrice, String message) {
    sendTweet(message);
    Account account = client.getAccount();
    // Find out how much free asset there is to trade
    Double freeBTC = Double.valueOf(account.getAssetBalance("BTC").getFree());
    Double freeBTCFloored = Math.floor(Double.valueOf(freeBTC) * 10000.0) / 10000.0;
    logger.info("Amount of BTC to trade: " + freeBTCFloored);
    try {
      logger.info("Executing sell of: " + freeBTCFloored + " BTC @ $" + sellPrice);
      // Submit the binance sell
      NewOrderResponse performSell =
              client.newOrder(
                      limitSell(
                              "BTCUSDT", TimeInForce.GTC, freeBTCFloored.toString(), sellPrice.toString()));
      logger.info("Trade submitted: " + performSell.getTransactTime());
    } catch (Exception e) {
      logger.error("There was an exception thrown during the sell?: " + e.getMessage());
      e.printStackTrace();
    }
    new CalcUtils().sleeper(3000);
    // Wait and make sure that the trade executed. If not, keep waiting
    List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
    logger.trace("Number of open BTCUSDT orders: " + openOrders.size());
    while (openOrders.size() > 0) {
      logger.trace("Orders for BTCUSDT are not empty, waiting 3 seconds...");
      new CalcUtils().sleeper(3000);
      openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
    }
    new CalcUtils().sleeper(3000);
    account = client.getAccount();
    // Verify that we have the correct amount of asset to trade
    Double freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
    // Loop until above 10.0 USDT
    while (freeUSDT < 10.0) {
      logger.trace("Looping because we currently have less than 10 USDT. Waiting 15 seconds...");
      new CalcUtils().sleeper(15000);
      freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
    }
    // Calculate and round the values in preparation for buying back
    Double freeUSDTFloored = Math.floor(freeUSDT * 100.0) / 100.0;
    Double BTCtoBuy = freeUSDTFloored / buyPrice;
    Double BTCtoBuyFloored = Math.floor(BTCtoBuy * 10000.0) / 10000.0;
    try {
      logger.info(
              "Executing buy with: "
                      + freeUSDTFloored
                      + " USDT @ $"
                      + buyPrice
                      + " = "
                      + BTCtoBuyFloored
                      + " BTC");
      // Submit the Binance buy back
      NewOrderResponse performBuy =
              client.newOrder(
                      limitBuy(
                              "BTCUSDT", TimeInForce.GTC, BTCtoBuyFloored.toString(), buyPrice.toString()));
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
    // Cancel all open orders
    List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
    for (Order order : openOrders) {
      logger.info("Cancelling order: " + order.getOrderId());
      client.cancelOrder(new CancelOrderRequest("BTCUSDT", order.getOrderId()));
    }
    // Execute market buy back
    new CalcUtils().sleeper(3000);
    Account account = client.getAccount();
    // Find out how much free asset there is to trade
    Double freeUSDT = Double.valueOf(account.getAssetBalance("USDT").getFree());
    Double freeUSDTFloored = Math.floor(freeUSDT * 100.0) / 100.0;

    TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");
    Double lastPrice = Double.valueOf(tickerStatistics.getLastPrice());

    Double BTCtoBuy = freeUSDTFloored / lastPrice;
    Double BTCtoBuyFloored = Math.floor(BTCtoBuy * 10000.0) / 10000.0;

    String message = "Executing market buy back of " + BTCtoBuyFloored + " BTC @ $" + lastPrice;
    logger.info(message);
    sendTweet(message);
    client.newOrder(marketBuy("BTCUSDT", BTCtoBuyFloored.toString()));
    new CalcUtils().sleeper(15000);
  }

  /**
   * Function to send a tweet. Pass in the message to send and it will use the preconfigured Twitter
   * OAuth credentials.
   *
   * @param message The message to tweet
   */
  private void sendTweet(String message) {
    // Use OAuth to pass Twitter credentials
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret);
    TwitterFactory tf = new TwitterFactory(cb.build());
    Twitter twitter = tf.getInstance();
    // Tweets can only be 280 characters long error if longer
    if (message.length() <= 280) {
      try {
        twitter.updateStatus(message);
        // My bad I was sending a tweet
        logger.trace("Sent tweet to @WestworldWyatt");
      } catch (TwitterException e) {
        logger.error("ERROR SENDING TWEET: Reason: {}", e);
      }
    } else {
      logger.error("Tweet too long!! (That's what she said)");
    }
  }

  /**
   * Report that the system is in developer mode
   */
  private void reportDevMode() {
    logger.error("Wyatt is currently in development mode! Not performing trades or tweets");
  }
}
