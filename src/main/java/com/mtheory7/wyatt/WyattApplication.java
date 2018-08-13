package com.mtheory7.wyatt;

import com.mtheory7.wyatt.mind.Wyatt;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WyattApplication {
  private final static Logger logger = Logger.getLogger(WyattApplication.class);

  public static void main(String[] args) {
    logger.info("Starting WYATT (v5.1.7) ...");
    if (args.length < 6) {
      logger.error("Not enough arguments have been given");
      System.exit(-1);
    }
    ConfigurableApplicationContext context = SpringApplication.run(WyattApplication.class, args);
    Wyatt wyatt = context.getBean(Wyatt.class);
    wyatt.startTrading();
  }
}
