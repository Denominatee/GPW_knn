package gpw.pl.tsi.classifier.tsi;

import gpw.pl.tsi.classifier.trainingset.TsiModelProperties;
import gpw.pl.tsi.classifier.tsi.datastructure.TimeSeriesResult;
import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNode;
import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNodeFactory;
import gpw.pl.tsi.utils.Interval;

import java.util.concurrent.TimeUnit;

public class TsiModel {

    private TsiTreeNode tree = null;
    private Interval interval;
    private int length;
    private int numMeans = 16;
    private int numNodeClusters = 4;
    private int dtwWindow = 500;
    private int clusterCapacity = 8;
    private int trainingSetSize;

    public TsiModel(TsiModelProperties properties) {
        this.interval = properties.getInterval();
        this.length = properties.getLength();
        this.numMeans = properties.getNumMeans();
        this.numNodeClusters = properties.getNumNodeClusters();
        this.dtwWindow = properties.getDtwWindow();
        this.clusterCapacity = properties.getClusterCapacity();
    }

    public void train(TimeSeries[] trainingSet) {
        long startTime = System.nanoTime();
        TsiTreeNodeFactory tsiTreeNodeFactory = new TsiTreeNodeFactory();
        tsiTreeNodeFactory.setNumMeans(numMeans);
        tsiTreeNodeFactory.setNumOfClusters(numNodeClusters);
        tsiTreeNodeFactory.setW(dtwWindow);
        tsiTreeNodeFactory.setClusterCapacity(clusterCapacity);
        tree = tsiTreeNodeFactory.buildNode(trainingSet);
        tree.setRoot();
        trainingSetSize = tree.getSize();
        //System.out.println("Training time " + ((System.nanoTime() - startTime) / 1000000000));
    }

    public TimeSeriesResult[] knn(double[] query, int k) {
        long startTime = System.nanoTime();
        if (tree == null){
            System.out.println("First train the model.");
            return null;
        }
        TsiKnnFinder knnFinder = new TsiKnnFinder(tree,dtwWindow);
        TimeSeriesResult[] knn = knnFinder.find(query, k);
        long end = System.nanoTime();
        long elapsedTime = end - startTime;
        long convert = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        System.out.println("Knn search time " + convert + " seconds");
        return knn;
    }

    public TsiTreeNode getTree() {
        return tree;
    }

    public void setTree(TsiTreeNode tree) {
        this.tree = tree;
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

    public int getTrainingSetSize() {
        return trainingSetSize;
    }
}