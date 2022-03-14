package gpw.pl.tsi.classifier.trainingset;

import gpw.pl.tsi.utils.Interval;

public class
TsiModelProperties {

    private Interval interval;
    private int length;
    private int numMeans;
    private int numNodeClusters;
    private int dtwWindow;
    private int clusterCapacity;

    public TsiModelProperties(Interval interval, int length,
                              int numMeans, int numNodeClusters,
                              int dtwWindow, int clusterCapacity) {
        this.interval = interval;
        this.length = length;
        this.numMeans = numMeans;
        this.numNodeClusters = numNodeClusters;
        this.dtwWindow = dtwWindow;
        this.clusterCapacity = clusterCapacity;
    }

    public Interval getInterval() {
        return interval;
    }

    public int getLength() {
        return length;
    }

    public int getNumMeans() {
        return numMeans;
    }

    public int getNumNodeClusters() {
        return numNodeClusters;
    }

    public int getDtwWindow() {
        return dtwWindow;
    }

    public int getClusterCapacity() {
        return clusterCapacity;
    }
}
