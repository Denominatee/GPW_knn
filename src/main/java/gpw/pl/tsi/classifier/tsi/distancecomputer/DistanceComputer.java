package gpw.pl.tsi.classifier.tsi.distancecomputer;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;

public class DistanceComputer {

    private final static LB_Keogh lbKeoghComputer = new LB_Keogh();
    private final static DTW dtwComputer = new DTW();
    private final static DBA dbaComputer = new DBA();

    public final double[][] envelope(final double[] Q, final int w) {
        return lbKeoghComputer.envelope(Q, w);
    }

    public final double dtw(final double[] Q, final double[] C, final int w) {
        return dtwComputer.compute(Q, C, w);
    }
    public final double lb_keogh(final double[][] W, final double[] C) {
        return lbKeoghComputer.compute(W, C);
    }
    public final double[] dbaUpdate(double[] T, final TimeSeries[] sequences, final int w){
        return dbaComputer.update(T,sequences,w);
    }
}
