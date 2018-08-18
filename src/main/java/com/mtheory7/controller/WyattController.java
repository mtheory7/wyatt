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
    return new ResponseEntity<>(wyatt.getTotalBalance(), HttpStatus.OK);
  }

  @GetMapping(path = PATH_PROFIT)
  public ResponseEntity getTotalProfit() {
    logger.trace(PATH_PROFIT + RESPONSE_SUFFIX);
    return new ResponseEntity<>(wyatt.getTotalProfit(), HttpStatus.OK);
  }

  @GetMapping(path = PATH_SHUTDOWN, params = {"pass"})
  public void seppuku(@RequestParam("pass") String pass, HttpServletRequest request) {
    logger.trace(PATH_SHUTDOWN + RESPONSE_SUFFIX);
    //Verify the password provided...
    String sha256hex = Hashing.sha256()
            .hashString(pass, StandardCharsets.UTF_8)
            .toString();
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
    String response = "=====  >>>>>  WYATT  <<<<<  =====<br>";
    if (Wyatt.DEVELOPMENT_MODE) response += "<br>### DEVELOPMENT MODE ###<br>";
    response += "<br>Status  :::  " + wyatt.getCurrentStateString();
    response += "<br><br>--- Prices ---";
    response += "<br>Current price: $" + wyatt.getCurrentPrice();
    response += "<br>Current target: $" + wyatt.getCurrentTargetPrice();
    if (wyatt.currentState) response += "<br>Buy back price: $" + wyatt.getCurrentBuyBackPrice();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping(path = PATH_OPEN_ORDERS)
  public ResponseEntity getOpenOrders() {
    logger.trace(PATH_OPEN_ORDERS + RESPONSE_SUFFIX);
    return new ResponseEntity<>(new Gson().toJson(wyatt.getOpenOrders()), HttpStatus.OK);
  }
}
