package gpw.pl.tsi.classifier.tsi.datastructure.cluster;

import java.sql.Timestamp;

public class TimeSeries {

    private double[] values;
    private String stock;
    private Timestamp start;
    private Timestamp stop;

    public TimeSeries(double[] values, String stock, Timestamp start, Timestamp stop) {
        this.values = values;
        this.stock = stock;
        this.start = start;
        this.stop = stop;
    }

    public double[] getValues() {
        return values;
    }

    public String getStock() {
        return stock;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getStop() {
        return stop;
    }

    public String getValuesString(){
        if (values == null)
            return null;
        StringBuffer sb = new StringBuffer("");
        sb.append("\'{");
        for (int i=0;i<values.length;i++) {
            sb.append(values[i]);
            if (i + 1 < values.length)
                sb.append(", ");
        }
        sb.append("}\'");
        return sb.toString();
    }
}
