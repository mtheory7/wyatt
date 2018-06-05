package model.data;

import model.DataIdentifier;

public class AverageData {
	private Double openAvg = 0.0;
	private Double closeAvg = 0.0;
	private Double lowAvg = 0.0;
	private Double highAvg = 0.0;
	private DataIdentifier identifier = null;

	public AverageData(DataIdentifier identifier) {
		this.identifier = identifier;
	}

	public Double getOpenAvg() {
		return openAvg;
	}

	public void setOpenAvg(Double openAvg) {
		this.openAvg = openAvg;
	}

	public Double getCloseAvg() {
		return closeAvg;
	}

	public void setCloseAvg(Double closeAvg) {
		this.closeAvg = closeAvg;
	}

	public Double getLowAvg() {
		return lowAvg;
	}

	public void setLowAvg(Double lowAvg) {
		this.lowAvg = lowAvg;
	}

	public Double getHighAvg() {
		return highAvg;
	}

	public void setHighAvg(Double highAvg) {
		this.highAvg = highAvg;
	}

	public DataIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(DataIdentifier identifier) {
		this.identifier = identifier;
	}
}
