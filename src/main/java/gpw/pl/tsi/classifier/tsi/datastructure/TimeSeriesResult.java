package gpw.pl.tsi.classifier.tsi.datastructure;

import java.sql.Timestamp;

public class TimeSeriesResult implements Comparable<TimeSeriesResult>{

    private double[] data;
    private String label;
    private double distance;
    private Timestamp start;
    private Timestamp stop;

    public TimeSeriesResult(double[] value, String label, double dtwDistance, Timestamp start, Timestamp stop) {
        this.data = value;
        this.label = label;
        this.distance = dtwDistance;
        this.start = start;
        this.stop = stop;
    }

    public double[] getData() {
        return data;
    }
    public void setData(double[] data) {
        this.data = data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(TimeSeriesResult o) {
        double compare = o.getDistance();
        if (this.distance < compare)
            return -1;
        if (this.distance > compare)
            return 1;
        return 0;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getStop() {
        return stop;
    }
}
