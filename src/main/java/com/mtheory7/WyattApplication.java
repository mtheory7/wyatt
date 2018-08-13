package com.mtheory7;

import com.mtheory7.wyatt.mind.Wyatt;
import com.mtheory7.wyatt.utils.CalcUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WyattApplication {
  private final static Logger logger = Logger.getLogger(WyattApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(WyattApplication.class, args);
    logger.info("Starting WYATT (v6.0.0) ...");
    if (args.length < 6) {
      logger.error("Not enough arguments have been given");
      System.exit(-1);
    }
    logger.info("Starting Wyatt trading...");
    for (;;) {
      Wyatt dolores = new Wyatt(args[0], args[1]);
      dolores.setTwitterCreds(args[2], args[3], args[4], args[5]);
      dolores.gatherMindData();
      dolores.predictAndTrade();
      dolores.printBalances();
      new CalcUtils().sleeper(25000);
    }
  }
}
