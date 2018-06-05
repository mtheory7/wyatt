package model.data;

public class PredictionData {

    public MindData mindData;
    public Double sellConfidencePercentage;
    public Double sellPrice;
    public Double buyBackAfterThisPercentage;

    public PredictionData(MindData mindData) {
        this.mindData = mindData;
    }

    public void calculatePredictionData() {

    }
}
