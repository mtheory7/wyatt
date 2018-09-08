package com.mtheory7.controller;

import com.google.common.hash.Hashing;
import com.mtheory7.wyatt.mind.Wyatt;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
public class WyattController {
  private static final Logger logger = Logger.getLogger(WyattController.class);
  private static final String PATH_BALANCE = "/balance/btc";
  private static final String PATH_PROFIT = "/balance/profit";
  private static final String PATH_SHUTDOWN = "/seppuku";
  private static final String PATH_STATUS = "/status";
  private static final String PATH_ORDER_HISTORY = "/orders";
  private static final String RESPONSE_SUFFIX = " endpoint hit";
  private final Wyatt wyatt;

  @Autowired
  public WyattController(Wyatt wyatt) {
    this.wyatt = wyatt;
  }

  @GetMapping(path = PATH_BALANCE)
  public ResponseEntity getTotalBTC() {
    logger.trace(PATH_BALANCE + RESPONSE_SUFFIX);
    return new ResponseEntity<>(wyatt.getCurrentBalance(), HttpStatus.OK);
  }

  @GetMapping(path = PATH_PROFIT)
  public ResponseEntity getTotalProfit() {
    logger.trace(PATH_PROFIT + RESPONSE_SUFFIX);
    return new ResponseEntity<>(wyatt.getCurrentProfit(), HttpStatus.OK);
  }

  @GetMapping(
      path = PATH_SHUTDOWN,
      params = {"pass"})
  public void seppuku(@RequestParam("pass") String pass, HttpServletRequest request) {
    logger.trace(PATH_SHUTDOWN + RESPONSE_SUFFIX);
    // Verify the password provided...
    String sha256hex = Hashing.sha256().hashString(pass, StandardCharsets.UTF_8).toString();
    if (sha256hex.equals("bc159b2d00a17af10d15f85c0fc3050626a9de62ddada278c086b5a53c883464")) {
      logger.info("Shutdown received from IP-address: " + request.getRemoteUser());
      System.exit(-1);
    } else {
      logger.info("Incorrect shutdown code from IP-address: " + request.getRemoteAddr());
    }
  }

  @GetMapping(path = PATH_STATUS)
  public ResponseEntity getState() {
    long startTime = System.nanoTime();
    Double currentPrice = wyatt.getCurrentPrice();
    Double initialInvestment = wyatt.getInitialInvestment();
    Double currentBalance = Double.valueOf(wyatt.getCurrentBalance());
    Double portfolioValue = currentBalance * currentPrice;
    Double balanceDiff = currentBalance - initialInvestment;
    Double balanceDiffUSD = balanceDiff * currentPrice;
    balanceDiff = Math.round(balanceDiff * 100000000.0) / 100000000.0;
    balanceDiffUSD = Math.round(balanceDiffUSD * 100.0) / 100.0;
    String response =
        "`Mb(<m>......</m>db<m>......</m></custom1>)d'<m>.................................</m></custom1><br>"
            + "<m>.</m>YM<m>......</m>,PM<m>......</m>,P<m>......................</m>/<m>......</m>/<m>....</m><br>"
            + "<m>.</m>`Mb<m>.....</m>d'Mb<m>.....</m>d'<m>.</m>____<m>....</m>___<m>...</m>___<m>...</m>/M<m>.....</m>/M<m>....</m><br>"
            + "<m>..</m>YM<m>....</m>,P<m>.</m>YM<m>....</m>,P<m>..</m>`MM(<m>....</m>)M'<m>.</m>6MMMMb<m>.</m>/MMMMM<m>.</m>/MMMMMM<br>"
            + "<m>..</m>`Mb<m>...</m>d'<m>.</m>`Mb<m>...</m>d'<m>...</m>`Mb<m>....</m>d'<m>.</m>8M'<m>..</m>`Mb<m>.</m>MM<m>.....</m>MM<m>....</m><br>"
            + "<m>...</m>YM<m>..</m>,P<m>...</m>YM<m>..</m>,P<m>.....</m>YM<m>...</m>,P<m>......</m>,oMM<m>.</m>MM<m>.....</m>MM<m>....</m><br>"
            + "<m>...</m>`Mb<m>.</m>d'<m>...</m>`Mb<m>.</m>d'<m>......</m>MM<m>..</m>M<m>...</m>,6MM9'MM<m>.</m>MM<m>.....</m>MM<m>....</m><br>"
            + "<m>....</m>YM,P<m>.....</m>YM,P<m>.......</m>`Mbd'<m>...</m>MM'<m>...</m>MM<m>.</m>MM<m>.....</m>MM<m>....</m><br>"
            + "<m>....</m>`MM'<m>.....</m>`MM'<m>........</m>YMP<m>....</m>MM<m>...</m>,MM<m>.</m>YM<m>...</m>,<m>.</m>YM<m>...</m>,<br>"
            + "<m>.....</m>YP<m>.......</m>YP<m>..........</m>M<m>.....</m>`YMMM9'Yb<m>.</m>YMMM9<m>..</m>YMMM9<br>"
            + "<m>.........................</m>d'<m>...........................</m><br>"
            + "<m>.....................</m>C3P,O<m>............................</m><br>"
            + "<m>......................</m>YMM<m>....................</m>v"
            + wyatt.getVersion()
            + "<m>...</m><br>";

    if (Wyatt.DEVELOPMENT_MODE) response += "<br>### DEVELOPMENT MODE ###";
    response += "<br>--- Status report ---";
    response += "<br>Status: " + wyatt.getCurrentStateString();
    response += "<br>Investment: " + initialInvestment + " BTC";
    response +=
        "<br>Portfolio  ≈ "
            + currentBalance
            + " BTC ($"
            + String.format("%.2f", portfolioValue)
            + ")";
    response += wyatt.getBalances();
    response +=
        "<br>Profit: "
            + wyatt.getCurrentProfit()
            + "% ("
            + String.format("%.8f", balanceDiff)
            + " BTC ≈ $"
            + String.format("%.2f", balanceDiffUSD)
            + ")";
    if (!wyatt.isEXECUTE_TWEETS()) {
      response += "<br>Tweeting: DISABLED";
    }
    response += "<br><br>--- Market ---";
    response += "<br>BTC Price: $" + String.format("%.2f", currentPrice);
    response += "<br>Target: $" + String.format("%.2f", wyatt.getCurrentTargetPrice());
    response += "<br>Buy back: $" + String.format("%.2f", wyatt.getCurrentBuyBackPrice());
    response += "<br>Sell confidence: " + wyatt.getCurrentSellConfidence() + "%";
    if (!wyatt.currentState) {
      Double diff = wyatt.getCurrentPrice() - wyatt.getOpenBuyBackPrice();
      response += "<br><br>--- Open buy back ---";
      response +=
          "<br>Amount: "
              + wyatt.getOpenBuyBackAmt()
              + " BTC @ $"
              + String.format("%.2f", wyatt.getOpenBuyBackPrice());
      response +=
          "<br>Difference: $"
              + String.format("%.2f", diff)
              + " ("
              + wyatt.getOpenBuyBackPercentage()
              + "%)";
    }
    response += "<br><br>--- Links ---";
    response +=
        "<br><a href=\"https://github.com/mtheory7/wyatt\" style=\"color:#F7931A\">Source Code</a>";
    response +=
        "<br><a href=\"https://twitter.com/WestworldWyatt\" style=\"color:#F7931A\">Twitter</a>";
    response +=
        "<br><a href=\"https://www.mtheory7.com/full.php\" style=\"color:#F7931A\">Full log</a>";
    response +=
        "<br><a href=\"http://www.mtheory7.com:17071/orders\" style=\"color:#F7931A\">Order History</a>";
    response += "<br><br>--- Donate ---";
    response +=
        "<br>Personal: <a href=\"https://www.blockchain.com/btc/address/"
            + "14Xqn75eLQVZEgjFgrQzF8C2PxNDf894yj\" style=\"color:#F7931A\">14X...4yj</a>";
    response +=
        "<br>Wyatt: <a href=\"https://www.blockchain.com/btc/address/"
            + "1BWu4LtW1swREcDWffFHZSuK3VTT1iWuba\" style=\"color:#F7931A\">1BW...uba</a>";
    logger.trace("*** EXITING getState() ...");
    long duration = (System.nanoTime() - startTime);
    logger.debug("Execution of /status endpoint took " + duration + " nanoseconds");
    return new ResponseEntity<>(
        "<html>"
            + "<head>"
            + "<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"https://www.mtheory7.com/apple-touch-icon.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://www.mtheory7.com/favicon-32x32.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"https://www.mtheory7.com/favicon-16x16.png\">"
            + "<link rel=\"manifest\" href=\"https://www.mtheory7.com/site.webmanifest\">"
            + "<link rel=\"mask-icon\" href=\"https://www.mtheory7.com/safari-pinned-tab.svg\" color=\"#5bbad5\">"
            + "<meta name=\"msapplication-TileColor\" content=\"#da532c\">"
            + "<meta name=\"theme-color\" content=\"#ffffff\">"
            + "<meta http-equiv=\"refresh\" content=\"25\" />"
            + "<style>"
            + "body {"
            + "  color: #F7931A;"
            + "}"
            + "m {"
            + "  color: #404040;"
            + "}"
            + "</style>"
            + "</head>"
            + "<title>Wyatt</title>"
            + "<body bgcolor=\"#000000\">"
            + "<font face=\"Courier\" size=\"3\">"
            + response
            + "</font>"
            + "</body>"
            + "</html>",
        HttpStatus.OK);
  }

  @GetMapping(path = PATH_ORDER_HISTORY)
  public ResponseEntity getOrderHistory() {
    logger.trace(PATH_ORDER_HISTORY + RESPONSE_SUFFIX);
    String response = wyatt.getOrderHistory();
    return new ResponseEntity<>(
        "<html>"
            + "<head>"
            + "<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"https://www.mtheory7.com/apple-touch-icon.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://www.mtheory7.com/favicon-32x32.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"https://www.mtheory7.com/favicon-16x16.png\">"
            + "<link rel=\"manifest\" href=\"https://www.mtheory7.com/site.webmanifest\">"
            + "<link rel=\"mask-icon\" href=\"https://www.mtheory7.com/safari-pinned-tab.svg\" color=\"#5bbad5\">"
            + "<meta name=\"msapplication-TileColor\" content=\"#da532c\">"
            + "<meta name=\"theme-color\" content=\"#ffffff\">"
            + "<meta http-equiv=\"refresh\" content=\"25\" />"
            + "</head>"
            + "<title>Wyatt</title>"
            + "<body bgcolor=\"#000000\">"
            + "<font face=\"Courier\" size=\"3\" color=\"#F7931A\">"
            + "<a href=\"http://www.mtheory7.com:17071/status\" style=\"color:#F7931A\">Back</a>"
            + response
            + "</font>"
            + "</body>"
            + "</html>",
        HttpStatus.OK);
  }
}
