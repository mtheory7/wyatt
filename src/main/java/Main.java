import mind.Wyatt;
import model.data.MindData;
import model.data.PredictionData;

public class Main {
	public static void main(String[] args) {
		Wyatt dolores = new Wyatt();
		//Wyatt.playSweetWater();
		MindData theCradle = dolores.gatherData();
		PredictionData predictionData = new PredictionData(theCradle);
		predictionData.calculatePredictionData();
	}
}