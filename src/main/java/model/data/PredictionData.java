package model.data;

import java.util.ArrayList;
import java.util.List;

public class PredictionData {

    public static Double buyBackAfterThisPercentage = 0.98;
    public Double sellConfidencePercentage;
    public Double sellPrice;
    public List<AverageData> averageData;

    public PredictionData() {
        averageData = new ArrayList<AverageData>();
    }

    public void calculatePredictionData() {

    }
}
