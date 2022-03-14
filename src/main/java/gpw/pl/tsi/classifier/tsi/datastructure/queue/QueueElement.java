package gpw.pl.tsi.classifier.tsi.datastructure.queue;

import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNode;

public class QueueElement implements Comparable<QueueElement>{

    private TsiTreeNode node;
    private double distance;

    public QueueElement(TsiTreeNode node, double distance) {
        this.node = node;
        this.distance = distance;
    }

    public TsiTreeNode getNode() {
        return node;
    }

    public void setNode(TsiTreeNode node) {
        this.node = node;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(QueueElement o) {
        double compare = o.getDistance();
        if (this.distance < compare)
            return -1;
        if (this.distance > compare)
            return 1;
        return 0;
    }
}
