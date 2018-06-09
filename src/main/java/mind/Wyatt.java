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
import utils.CalcUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.binance.api.client.domain.account.NewOrder.limitBuy;
import static com.binance.api.client.domain.account.NewOrder.limitSell;

public class Wyatt {

    final static Logger logger = Logger.getLogger(Wyatt.class);
    private static Double percentageRatio = 1.00165;
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
        Double sellPrice = 0.0;
        for (AverageData averageData : predictionData.averageData) {
            //total += (averageData.getCloseAvg()+averageData.getHighAvg())/2*percentageRatio;
            if (averageData.getNumberOfNodesAveraged() == 5)
                sellPrice += (averageData.getCloseAvg() + averageData.getHighAvg()) / 2 * percentageRatio;
        }
        sellPrice = Math.round(sellPrice * 100.0) / 100.0;
        Double buyBack = Math.round(sellPrice * predictionData.buyBackAfterThisPercentage * 100.0) / 100.0;
        TickerStatistics lastPrice = null;
        for (HashMap.Entry<DataIdentifier, TickerStatistics> entry : mindData.getLastPriceData().entrySet()) {
            if (entry.getKey().getInterval() == CandlestickInterval.ONE_MINUTE
                    && entry.getKey().getTicker().equals("BTCUSDT")) {
                lastPrice = entry.getValue();
            }
        }
        Double lastPriceFloored = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
        logger.trace("Current: $" + lastPriceFloored + " Sell: $" + sellPrice + " Buy: $" + buyBack);
        boolean trade = true;
        List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
        if (openOrders.size() > 0) {
            logger.trace("Orders for BTCUSDT are not empty, not trading for 120 seconds...");
            trade = false;
            new CalcUtils().sleeper(120000);
        }
        if (Double.valueOf(lastPrice.getLastPrice()) > sellPrice && trade) {
            Double z = Math.round(Double.valueOf(lastPrice.getLastPrice()) * 100.0) / 100.0;
            //WE SHOULD SELL AND BUY!
            logger.info("\nDeciding to sell! Target price: $" + sellPrice + ". Current price: $" + z + ". Buy back price: " + buyBack + "\n");
            performSellAndBuyBack(z, buyBack);
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
        logger.trace("Amount of BTC to trade: " + freeBTCFloored);
        try {
            logger.info("Executing sell of: " + freeBTCFloored + " BTC @ $" + sellPrice);
            NewOrderResponse performSell = client.newOrder(
                    limitSell("BTCUSDT", TimeInForce.GTC, freeBTCFloored.toString(), sellPrice.toString()));
            logger.trace("Trade submitted: " + performSell.getTransactTime());
        } catch (Exception e) {
            logger.error("There was an exception thrown during the sell?: " + e.getMessage());
            e.printStackTrace();
        }
        new CalcUtils().sleeper(3000);
        List<Order> openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
        logger.info("Number of open BTCUSDT orders: " + openOrders.size());
        while (openOrders.size() > 0) {
            logger.info("Orders for BTCUSDT are not empty, waiting 3 seconds...");
            new CalcUtils().sleeper(3000);
            openOrders = client.getOpenOrders(new OrderRequest("BTCUSDT"));
        }
        new CalcUtils().sleeper(3000);
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
            logger.trace("Trade submitted: " + performBuy.getTransactTime());
        } catch (Exception e) {
            logger.error("There was an exception thrown during the buy?: " + e.getMessage());
            e.printStackTrace();
        }
        new CalcUtils().sleeper(3000);
    }
}