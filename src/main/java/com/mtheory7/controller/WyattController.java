package com.mtheory7.controller;

import com.google.common.collect.EvictingQueue;
import com.google.common.hash.Hashing;
import com.mtheory7.wyatt.mind.Wyatt;
import com.mtheory7.wyatt.utils.CalcUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

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
  public int serverPort;
  public String wyattBTCAddress;
  public String donateBTCAddress;
  public String hostIP;
  public String mainColor;
  private Queue<Double> queue = EvictingQueue.create(100);

  @Autowired
  public WyattController(Wyatt wyatt) {
    this.wyatt = wyatt;
  }

  @Value("${mainColor}")
  public void getMainColor(String color) {
    this.mainColor = color;
  }

  @Value("${server.port}")
  public void getServerPort(int port) {
    this.serverPort = port;
  }

  @Value("${hostIP}")
  public void getHostIP(String ip) {
    this.hostIP = ip;
  }

  @Value("${wyattBTCAddress}")
  public void getWyattBTCAddress(String address) {
    this.wyattBTCAddress = address;
  }

  @Value("${donateBTCAddress}")
  public void getDonateBTCAddress(String address) {
    this.donateBTCAddress = address;
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
    if (confirmPassword(pass)) {
      logger.info("Shutdown received from IP-address: " + request.getRemoteUser());
      System.exit(-1);
    } else {
      logger.info("Incorrect shutdown code from IP-address: " + request.getRemoteAddr());
    }
  }

  @GetMapping(path = PATH_STATUS)
  public ResponseEntity getState() {
    double startTime = (double) System.nanoTime();
    Double currentPrice = wyatt.getCurrentPrice();
    Double initialInvestment = wyatt.getInitialInvestment();
    Double currentBalance = Double.valueOf(wyatt.getCurrentBalance());
    Double portfolioValue = currentBalance * currentPrice;
    double balanceDiff = CalcUtils.roundTo(currentBalance - initialInvestment, 8);
    double balanceDiffUSD = CalcUtils.roundTo(balanceDiff * currentPrice, 2);
    StringBuilder response =
        new StringBuilder(
            "M\"\"MMM\"\"MMM\"\"M&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dP&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dP<br>M&nbsp;&nbsp;MMM&nbsp;&nbsp;MMM&nbsp;&nbsp;M&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;88&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;88<br>M&nbsp;&nbsp;MMP&nbsp;&nbsp;MMP&nbsp;&nbsp;M&nbsp;dP&nbsp;&nbsp;&nbsp;&nbsp;dP&nbsp;.d8888b.&nbsp;d8888P&nbsp;d8888P<br>M&nbsp;&nbsp;MM'&nbsp;&nbsp;MM'&nbsp;.M&nbsp;88&nbsp;&nbsp;&nbsp;&nbsp;88&nbsp;88'&nbsp;&nbsp;`88&nbsp;&nbsp;&nbsp;88&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;88<br>M&nbsp;&nbsp;`'&nbsp;.&nbsp;''&nbsp;.MM&nbsp;88.&nbsp;&nbsp;.88&nbsp;88.&nbsp;&nbsp;.88&nbsp;&nbsp;&nbsp;88&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;88<br>M&nbsp;&nbsp;&nbsp;&nbsp;.d&nbsp;&nbsp;.dMMM&nbsp;`8888P88&nbsp;`88888P8&nbsp;&nbsp;&nbsp;dP&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dP<br>MMMMMMMMMMMMMM&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.88<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;d8888P<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    response.append("<m>Version&nbsp;").append(wyatt.getVersion()).append("</m><br>");
    if (Wyatt.DEVELOPMENT_MODE) {
      response.append("<br>### DEVELOPMENT MODE ###");
    }
    response.append("<br>--- Status report ---");
    response.append("<br>Status: ").append(wyatt.getCurrentStateString());
    response.append("<br>Investment: ").append(initialInvestment).append(" BTC");
    response
        .append("<br>Portfolio  ≈ ")
        .append(currentBalance)
        .append(" BTC ($")
        .append(String.format("%.2f", portfolioValue))
        .append(")");
    response.append(wyatt.getBalances());
    response
        .append("<br>Profit: ")
        .append(wyatt.getCurrentProfit())
        .append("% (")
        .append(String.format("%.8f", balanceDiff))
        .append(" BTC ≈ $")
        .append(String.format("%.2f", balanceDiffUSD))
        .append(")");
    if (!wyatt.isEXECUTE_TWEETS()) {
      response.append("<br>Tweeting: DISABLED");
    }
    response.append("<br><br>--- Market ---");
    response.append("<br>BTC Price: $").append(String.format("%.2f", currentPrice));
    response.append("<br>Target: $").append(String.format("%.2f", wyatt.getCurrentTargetPrice()));
    response
        .append("<br>Buy back: $")
        .append(String.format("%.2f", wyatt.getCurrentBuyBackPrice()));
    response.append("<br>Sell confidence: ").append(wyatt.getCurrentSellConfidence()).append("%");
    if (!wyatt.currentState) {
      Double diff = wyatt.getCurrentPrice() - wyatt.getOpenBuyBackPrice();
      response.append("<br><br>--- Open buy back ---");
      response
          .append("<br>Amount: ")
          .append(wyatt.getOpenBuyBackAmt())
          .append(" BTC @ $")
          .append(String.format("%.2f", wyatt.getOpenBuyBackPrice()));
      response
          .append("<br>Difference: $")
          .append(String.format("%.2f", diff))
          .append(" (")
          .append(wyatt.getOpenBuyBackPercentage())
          .append("%)");
    }
    response.append("<br><br>--- Links ---");
    response.append(
        "<br><a href=\"https://github.com/mtheory7/wyatt\" style=\"color:" + this.mainColor + "\">Source Code</a>");
    response.append(
        "<br><a href=\"https://twitter.com/WestworldWyatt\" style=\"color:" + this.mainColor + "\">Twitter</a>");
    response.append(
        "<br><a href=\"http://"
            + this.hostIP
            + ":"
            + this.serverPort
            + "/orders\" style=\"color:" + this.mainColor + "\">Order History</a>");
    response.append("<br><br>--- Donate ---");
    response.append(
        "<br>Personal: <a href=\"https://www.blockchain.com/btc/address/"
            + this.donateBTCAddress
            + "\" style=\"color:" + this.mainColor + "\">"
            + this.donateBTCAddress
            + "</a>");
    response.append(
        "<br>Wyatt: <a href=\"https://www.blockchain.com/btc/address/"
            + this.wyattBTCAddress
            + "\" style=\"color:" + this.mainColor + "\">"
            + this.wyattBTCAddress
            + "</a>");
    queue.add((System.nanoTime() - startTime) / 1000000000);
    response
        .append("<g><br><br>Avg load time: ")
        .append(String.format("%.4f", getAverageStatusLoadTime()))
        .append("s");
    response.append("<br>Uptime: ").append(CalcUtils.getUpTimeString()).append("</g>");
    return new ResponseEntity<>(
        new StringBuilder(
                "<html><head><link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"https://"
                    + this.hostIP
                    + "/apple-touch-icon.png\"><link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://"
                    + this.hostIP
                    + "/favicon-32x32.png\"><link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"https://"
                    + this.hostIP
                    + "/favicon-16x16.png\"><link rel=\"manifest\" href=\"https://"
                    + this.hostIP
                    + "/site.webmanifest\"><link rel=\"mask-icon\" href=\"https://"
                    + this.hostIP
                    + "/safari-pinned-tab.svg\" color=\"#5bbad5\"><meta name=\"msapplication-TileColor\" content=\"#da532c\"><meta name=\"theme-color\" content=\"#ffffff\"><meta http-equiv=\"refresh\" content=\"25\" /><style>body {  color: " + this.mainColor + ";}m {  color: #A9A9A9;}g {  color: #999999;}</style></head><title>Wyatt</title><body bgcolor=\"#000000\"><font face=\"Courier\" size=\"3\">")
            .append(response)
            .append("</font></body></html>"),
        HttpStatus.OK);
  }

  @GetMapping(path = PATH_ORDER_HISTORY)
  public ResponseEntity getOrderHistory() {
    logger.trace(PATH_ORDER_HISTORY + RESPONSE_SUFFIX);
    String response = wyatt.getOrderHistory();
    return new ResponseEntity<>(
        "<html>"
            + "<head>"
            + "<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"https://"
            + this.hostIP
            + "/apple-touch-icon.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://"
            + this.hostIP
            + "/favicon-32x32.png\">"
            + "<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"https://"
            + this.hostIP
            + "/favicon-16x16.png\">"
            + "<link rel=\"manifest\" href=\"https://"
            + this.hostIP
            + "/site.webmanifest\">"
            + "<link rel=\"mask-icon\" href=\"https://"
            + this.hostIP
            + "/safari-pinned-tab.svg\" color=\"#5bbad5\">"
            + "<meta name=\"msapplication-TileColor\" content=\"#da532c\">"
            + "<meta name=\"theme-color\" content=\"#ffffff\">"
            + "<meta http-equiv=\"refresh\" content=\"25\" />"
            + "</head>"
            + "<title>Wyatt</title>"
            + "<body bgcolor=\"#000000\">"
            + "<font face=\"Courier\" size=\"3\" color=\"" + this.mainColor + "\">"
            + "<a href=\"http://"
            + this.hostIP
            + ":"
            + this.serverPort
            + "/status\" style=\"color:" + this.mainColor + "\">Back</a>"
            + response
            + "</font>"
            + "</body>"
            + "</html>",
        HttpStatus.OK);
  }

  /**
   * Returns the average of the queue
   *
   * @return Double average
   */
  private Double getAverageStatusLoadTime() {
    if (queue.size() == 0) {
      return null;
    }
    double average = 0.0;
    for (Double num : queue) {
      average += num / queue.size();
    }
    return average;
  }

  /**
   * Returns the result of comparing the password with the supplied key
   *
   * @param pass The user supplied password to check
   * @return Whether or not the user's password was correct
   */
  private boolean confirmPassword(String pass) {
    return Hashing.sha256()
        .hashString(pass, StandardCharsets.UTF_8)
        .toString()
        .equals("bc159b2d00a17af10d15f85c0fc3050626a9de62ddada278c086b5a53c883464");
  }
}
