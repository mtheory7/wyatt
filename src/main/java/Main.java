import mind.Wyatt;
import org.apache.log4j.Logger;
import utils.CalcUtils;

public class Main {
    final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting WYATT version 1.2.2 ...");
        if (args[0] == null || args[1] == null) {
            System.exit(-1);
            logger.error("Not enough arguments have been given");
        }
        if (args[0].equals("") || args[1].equals("")) {
            System.exit(-2);
            logger.error("Arguments cannot be empty");
        }
        for (; ; ) {
            Wyatt dolores = new Wyatt(args[0], args[1]);
            dolores.gatherMindData();
            dolores.gatherPredictionData();
            dolores.printBalances();
            new CalcUtils().sleeper(25000);
        }
    }
}