package com.mtheory7.controller;

import com.mtheory7.wyatt.mind.Wyatt;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WyattController {
  private static final Logger logger = Logger.getLogger(WyattController.class);
  private static final String PATH_BALANCE = "/totalBalance";
  private static final String PATH_SHUTDOWN = "/shutdown";
  private static final String PATH_STATE = "/state";
  private final Wyatt wyatt;

  @Autowired
  public WyattController(Wyatt wyatt) {
    this.wyatt = wyatt;
  }

  @RequestMapping(path = PATH_BALANCE, method = RequestMethod.GET)
  public ResponseEntity getTotalBalance() {
    logger.trace(PATH_BALANCE + " endpoint hit");
    return new ResponseEntity<>(wyatt.getTotalBalance(), HttpStatus.OK);
  }

  @RequestMapping(path = PATH_SHUTDOWN, method = RequestMethod.GET)
  public void shutdown() {
    logger.trace(PATH_SHUTDOWN + " endpoint hit");
    logger.info("Shutdown down now...");
    System.exit(-1);
  }

  @RequestMapping(path = PATH_STATE, method = RequestMethod.GET)
  public ResponseEntity getState() {
    logger.trace(PATH_STATE + " endpoint hit");
    String response = "Have you ever seen anything so full of splendor?";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
