package com.mtheory7;

import com.mtheory7.wyatt.mind.Wyatt;
import com.mtheory7.wyatt.utils.CalcUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WyattApplication {
  private static final Logger logger = Logger.getLogger(WyattApplication.class);
  private static final String VERSION = "6.6.4";

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(WyattApplication.class, args);
    logger.info("Starting WYATT (v" + VERSION + ") ...");
    if (args.length < 6) {
      logger.error("Not enough arguments have been given");
      System.exit(-1);
    }
    Wyatt dolores = context.getBean(Wyatt.class);
    dolores.setBinanceCreds(args[0], args[1]);
    dolores.setTwitterCreds(args[2], args[3], args[4], args[5]);
    for (; ; ) {
      dolores.gatherMindData();
      dolores.predictAndTrade();
      dolores.printBalances();
      dolores.reset();
      new CalcUtils().sleeper(25000);
    }
  }

  public static String getVersion() {
    return VERSION;
  }
}
