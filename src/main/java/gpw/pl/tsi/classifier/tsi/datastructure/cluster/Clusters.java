package gpw.pl.tsi.classifier.tsi.datastructure.cluster;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Clusters {

    private double[][] centroids;
    private int[] clusterSeed;
    private ArrayList<TimeSeries[]> clusters;
    private int numClusters;

    public Clusters(double[][] centroids, int[] clusterSeed) {
        this.centroids = centroids;
        this.clusterSeed = clusterSeed;
        this.clusters = new ArrayList<TimeSeries[]>();
        this.numClusters = centroids.length;
    }

    public void generateClusters(TimeSeries[] nodeData) {
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        for (int i=0;i<numClusters;i++){
            int finalI = i;
            Long clusterSize = Arrays.stream(clusterSeed).filter(k -> k == finalI).count();
            if (clusterSize == 0) {
                toRemove.add(i);
                continue;
            }
            TimeSeries[] cluster = new TimeSeries[clusterSize.intValue()];
            int index = 0;
            for (int j=0;j<clusterSeed.length;j++){
                if (clusterSeed[j] == i){
                    cluster[index++] = nodeData[j];
                }
            }
            clusters.add(cluster);
        }
        //remove empty clusters
        if (!toRemove.isEmpty()){
            numClusters = numClusters - toRemove.size();
            Integer[] objects = toRemove.toArray(new Integer[toRemove.size()]);
            int[] primitives = ArrayUtils.toPrimitive(objects);
            centroids = ArrayUtils.removeAll(centroids, primitives);
        }
    }
    public double[][] getCentroids() {
        return centroids;
    }

    public ArrayList<TimeSeries[]> getClusters() {
        return clusters;
    }

    public int getNumClusters() {
        return numClusters;
    }
}
