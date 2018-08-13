package com.mtheory7.controller;

import com.mtheory7.wyatt.mind.Wyatt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WyattController {
  private final Wyatt wyatt;

  @Autowired
  public WyattController(Wyatt wyatt) {
    this.wyatt = wyatt;
  }

  @RequestMapping(path = "/totalBalance", method = RequestMethod.GET)
  public ResponseEntity getTotalBalance() {
    return new ResponseEntity<>(wyatt.getTotalBalance(), HttpStatus.OK);
  }

  @RequestMapping(path = "/shutdown", method = RequestMethod.GET)
  public void shutdown() {
    System.exit(-1);
  }

  @RequestMapping(path = "/state", method = RequestMethod.GET)
  public ResponseEntity getState() {
    String response = "Have you ever seen anything so full of splendor?";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
