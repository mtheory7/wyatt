package com.mtheory7.controller;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
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
  private static final String PATH_OPEN_ORDERS = "/orders";
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
    logger.trace(PATH_STATUS + RESPONSE_SUFFIX);
    String response = "=====  >>>>>  WYATT (v" + wyatt.getVersion() + ") <<<<<  =====<br>";
    if (Wyatt.DEVELOPMENT_MODE) response += "<br>### DEVELOPMENT MODE ###";
    response += "<br>--- Status report ---";
    response += "<br>Status: " + wyatt.getCurrentStateString();
    response += "<br>Profit: " + wyatt.getCurrentProfit() + "%";
    response += "<br>Portfolio value: " + wyatt.getCurrentBalance() + " BTC";
    response += "<br>Initial investment: " + wyatt.getInitialInvestment() + " BTC";
    response += "<br>Sell confidence: " + wyatt.getCurrentSellConfidence() + "%";
    response += "<br><br>--- Prices ---";
    response += "<br>BTC Price: $" + wyatt.getCurrentPrice();
    response += "<br>Target: $" + wyatt.getCurrentTargetPrice();
    response += "<br>Buy back: $" + wyatt.getCurrentBuyBackPrice();
    response += "<br><br>--- Links ---";
    response += "<br><a href=\"https://github.com/mtheory7/wyatt\" style=\"color:#F7931A\">Source Code</a>";
    response += "<br><a href=\"https://twitter.com/WestworldWyatt\" style=\"color:#F7931A\">Wyatt's Twitter</a>";
    response += "<br><a href=\"https://www.mtheory7.com/full.php\" style=\"color:#F7931A\">Wyatt's full log</a>";
    if (!wyatt.currentState) {
      Double diff = wyatt.getCurrentPrice() - wyatt.getOpenBuyBackPrice();
      diff = Math.round(diff * 1000.0) / 1000.0;
      response += "<br><br>--- Open buy back ---";
      response +=
          "<br>Amount: " + wyatt.getOpenBuyBackAmt() + " BTC @ $" + wyatt.getOpenBuyBackPrice();
      response +=
          "<br>Difference: $"
              + diff
              + " ("
              + wyatt.getOpenBuyBackPercentage()
              + "%)";
    }
    response += "<br><br>--- Donate ---<br>";
    response +=
        "<a href=\"https://www.blockchain.com/btc/address/"
            + "14Xqn75eLQVZEgjFgrQzF8C2PxNDf894yj\" style=\"color:#F7931A\">14Xqn75eLQVZEgjFgrQzF8C2PxNDf894yj</a>";
    return new ResponseEntity<>(
        "<html>\n"
            + "<head>\n"
            + "<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"https://www.mtheory7.com/apple-touch-icon.png\">\n"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://www.mtheory7.com/favicon-32x32.png\">\n"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"https://www.mtheory7.com/favicon-16x16.png\">\n"
            + "<link rel=\"manifest\" href=\"https://www.mtheory7.com/site.webmanifest\">\n"
            + "<link rel=\"mask-icon\" href=\"https://www.mtheory7.com/safari-pinned-tab.svg\" color=\"#5bbad5\">\n"
            + "<meta name=\"msapplication-TileColor\" content=\"#da532c\">\n"
            + "<meta name=\"theme-color\" content=\"#ffffff\">\n"
            + "</head>\n"
            + "<title>Wyatt</title>\n"
            + "<body bgcolor=\"#000000\">\n"
            + "<font face=\"Courier\" size=\"3\" color=\"#F7931A\">\n"
            + response
            + "</font> \n"
            + "</body>\n"
            + "</html> ",
        HttpStatus.OK);
  }

  @GetMapping(path = PATH_OPEN_ORDERS)
  public ResponseEntity getOpenOrders() {
    logger.trace(PATH_OPEN_ORDERS + RESPONSE_SUFFIX);
    return new ResponseEntity<>(new Gson().toJson(wyatt.getOpenOrders()), HttpStatus.OK);
  }
}
