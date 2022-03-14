package gpw.pl.tsi.classifier.tsi.datastructure.tree;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.Clusters;
import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.classifier.tsi.distancecomputer.DistanceComputer;
import gpw.pl.tsi.classifier.tsi.distancecomputer.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TsiTreeNodeFactory {

    private int numMeans;
    private int numOfClusters;
    private int clusterCapacity;
    private int w;
    private final Tools tools = new Tools();
    private final DistanceComputer distComputer = new DistanceComputer();


    public TsiTreeNode buildNode(TimeSeries[] nodeData) {
        //Checking if reached leaf
        if (nodeData.length <= clusterCapacity) {
            TsiTreeNode node = new TsiTreeNode();
            node.setLeaf();
            node.setValues(nodeData);
            return node;
        }
        // split data for this node into clusters
        Clusters clusters = getClusters(nodeData);
        //If data cannot be split into clusters set leaf
        int numClusters = clusters.getNumClusters();
        if (numClusters == 1){
            TsiTreeNode node = new TsiTreeNode();
            node.setLeaf();
            node.setValues(clusters.getClusters().get(0));
            return node;
        }
        //Build node for each cluster
        double[][] centroids = clusters.getCentroids();
        TsiTreeNode node = new TsiTreeNode();
        for (int i = 0;i<centroids.length;i++){
            TsiTreeNode childNode = buildNode(clusters.getClusters().get(i));
            childNode.setCentroid(centroids[i]);
            node.addChild(childNode);
        }
        return node;
    }

    private Clusters getClusters(TimeSeries[] nodeData){
        Clusters clusters = choseRandomClusters(nodeData);
        ArrayList<TimeSeries[]> clusters1 = clusters.getClusters();
        for (TimeSeries[] el: clusters1
             ) {
            if (el.length == 0){
                System.out.println("Error empty cluster");
                clusters = choseRandomClusters(nodeData);
            }

        }
        for (int i = 0; i< numMeans; i++){
            // recalculate new center and cluster assignment
            Clusters newClusters = clusterMean(clusters, nodeData);
            if (Arrays.deepEquals(clusters.getCentroids(), newClusters.getCentroids())) {
                break;
            } else {
                clusters = newClusters;
            }
        }
        return clusters;
    }

    private Clusters choseRandomClusters(TimeSeries[] nodeData) {
        int size = nodeData.length;
        final double[][] centroids = new double[numOfClusters][nodeData[0].getValues().length];
        final int[] clusterSeed = new int[size];
        final double[] dist2NearCentroid = new double[size];		// distances to nearest centroid
        //1st centroid chosen randomly from training set
        int randIndex = tools.randInt(size);
        double[] centroid = nodeData[randIndex].getValues();
        centroids[0] = centroid;
        //Mark this index as taken
        List<Integer> taken = new ArrayList<>();
        taken.add(randIndex);
        //Calculate distances to first centroid
        for (int i = 0; i < size; i++) {
            if (i != randIndex) {
                dist2NearCentroid[i] = distComputer.dtw(centroid, nodeData[i].getValues(), w);
            }
        }
        double threshold = getThreshold(dist2NearCentroid);

        //choosing rest of centroids
        for (int i=1;i<numOfClusters;i++) {
            //searching for random centroid with sufficient distance from the rest of centroids
            while (true) {
                randIndex = tools.randInt(size);
                if (!taken.contains(randIndex) && dist2NearCentroid[randIndex] >= threshold)
                    break;
            }
            centroid = nodeData[randIndex].getValues();
            centroids[i] = centroid;
            //recalculating distances to nearest centroid and reassigning cluster seeds
            recalculateDistances(centroid,nodeData, taken,dist2NearCentroid,clusterSeed,i);
            taken.add(randIndex);
            //recalculating threshold for min distance
            threshold = getThreshold(dist2NearCentroid);
        }
        Clusters clusters = new Clusters(centroids, clusterSeed);
        clusters.generateClusters(nodeData);
        return clusters;
    }

    private void recalculateDistances(double[] centroid, TimeSeries[] nodeData, List<Integer> taken,
                                      double[] dist2NearCentroid, int[] clusterSeed, int i) {
        final double[][] wedge = distComputer.envelope(centroid, w);
        for (int j = 0; j < nodeData.length; j++) {
            if (!taken.contains(j)) {
                final double lbDistance = distComputer.lb_keogh(wedge, nodeData[j].getValues());
                if (lbDistance < dist2NearCentroid[j]){
                    final double dtwDistance = distComputer.dtw(centroid, nodeData[j].getValues(), w);
                    if (dtwDistance < dist2NearCentroid[j]) {
                        dist2NearCentroid[j] = dtwDistance;
                        clusterSeed[j] = i;
                    }
                }
            }
        }
    }

    private final Clusters clusterMean(Clusters clusters, TimeSeries[] nodeData) {
        int nClusters = clusters.getNumClusters();
        int size = clusters.getClusters().get(0).length;
        double[][] centroids = clusters.getCentroids();
        ArrayList<TimeSeries[]> clustersValues = clusters.getClusters();
        final double[][] newCentroids = new double[nClusters][size];
        for (int i = 0; i < nClusters; i++) {
            TimeSeries[] cluster = clustersValues.get(i);
            if (cluster.length == 0)
                continue;
            double[] newCentroid = distComputer.dbaUpdate(centroids[i].clone(),cluster,w);
            newCentroids[i] = newCentroid;
        }
        int[] newClusterSeed = getClusterSeeds(newCentroids, nodeData);
        Clusters newClusters = new Clusters(newCentroids, newClusterSeed);
        newClusters.generateClusters(nodeData);
        return newClusters;
    }

    private int[] getClusterSeeds(double[][] newCentroids, TimeSeries[] nodeData) {
        int[] clusterSeeds = new int[nodeData.length];
        for (int i = 0;i<clusterSeeds.length;i++) {
            double smallestDist = Double.POSITIVE_INFINITY;
            int bestFit = 0;
            TimeSeries doubles = nodeData[i];
            for (int j =0;j<newCentroids.length;j++){
                double[] centroid = newCentroids[j];
                final double dtwDistance = distComputer.dtw(centroid, doubles.getValues(), w);
                if (dtwDistance < smallestDist) {
                    smallestDist = dtwDistance;
                    bestFit = j;
                }
            }
            clusterSeeds[i]=bestFit;
        }
        return clusterSeeds;
    }

    private double getThreshold(double[] dist2NearCentroid) {
        double[] clone = dist2NearCentroid.clone();
        Arrays.sort(clone);
        int index = (int) Math.ceil(50 / 100.0 * clone.length);
        return clone[index];
    }

    public void setNumOfClusters(int numOfClusters) {
        this.numOfClusters = numOfClusters;
    }

    public void setClusterCapacity(int clusterCapacity) {
        this.clusterCapacity = clusterCapacity;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setNumMeans(int numMeans) {
        this.numMeans = numMeans;
    }
}