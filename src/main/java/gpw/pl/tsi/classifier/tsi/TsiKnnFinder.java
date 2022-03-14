package gpw.pl.tsi.classifier.tsi;

import gpw.pl.tsi.classifier.tsi.datastructure.TimeSeriesResult;
import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.classifier.tsi.distancecomputer.DistanceComputer;
import gpw.pl.tsi.classifier.tsi.datastructure.queue.QueueElement;
import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNode;

import java.sql.Timestamp;
import java.util.*;

public class TsiKnnFinder {

    private TsiTreeNode tree;
    private double[] query;
    private int k;
    private int dtwWindow;
    private PriorityQueue<QueueElement> dtwQueue = new PriorityQueue<>();
    private PriorityQueue<QueueElement> lbQueue = new PriorityQueue<>();
    private TreeSet<TimeSeriesResult> knn = new TreeSet<>();
    private boolean stop = false;
    private double[][] wedge;

    private final DistanceComputer distComputer = new DistanceComputer();

    public TsiKnnFinder(TsiTreeNode tree, int dtwWindow) {
        this.tree = tree;
        this.dtwWindow = dtwWindow;
    }

    public TimeSeriesResult[] find(double[] query, int k) {
        long startTime = System.nanoTime();
        this.query = query;
        this.k = k;
        wedge = distComputer.envelope(query, dtwWindow);
        /* Traverse the tree from root*/
        traverseTree(tree);
        while (!searchFinished()){
            /* Calculate dtw for nodes in lb queue if necessary */
            recalculateQueues();
            if(!dtwQueue.isEmpty()){
                /* Traverse the tree from node with smallest distance in dtwQueue */
                traverseTree(dtwQueue.poll().getNode());
            }
            if (((System.nanoTime() - startTime) / 1000000000.0) > 4)
                stop = true;
        }
        TimeSeriesResult[] stK = get1stK(knn, false);
        return stK;
    }

    private TimeSeriesResult[] get1stK(TreeSet<TimeSeriesResult> knn, boolean repetition) {
        TimeSeriesResult[] firstKElements = new TimeSeriesResult[k];
        Iterator<TimeSeriesResult> iterator = knn.iterator();
        List<String> labels = new ArrayList<>();
        int index = 0;
        while (index < k && iterator.hasNext()) {
            TimeSeriesResult next = iterator.next();
            if (repetition) {
                firstKElements[index++] = next;
            } else {
                String label = next.getLabel();
                if (!labels.contains(label)) {
                    firstKElements[index++] = next;
                    labels.add(label);
                }
            }
        }
        return firstKElements;
    }

    private void traverseTree(TsiTreeNode node){
        if (node.isLeaf()) {
            TimeSeries[] values = node.getValues();
            for (int i=0;i< values.length;i++){
                double[] value = values[i].getValues();
                String label = values[i].getStock();
                Timestamp start = values[i].getStart();
                Timestamp stop = values[i].getStop();
                double dtwDistance = distComputer.dtw(query, value, dtwWindow);
                knn.add(new TimeSeriesResult(value,label,dtwDistance,start,stop));
            }
        } else {
            int bestChildIdx = findBestChild(node);
            TsiTreeNode bestChild = node.getChildren().get(bestChildIdx);
            traverseTree(bestChild);
        }
    }

    private void recalculateQueues() {
        /* Calculating dtw for lb queue (and removing them from lb queue)
        until dtw queue has smaller distance then lb */
        while (true) {
            if (lbQueue.isEmpty())
                return;
            if (dtwQueue.isEmpty()) {
                QueueElement poll = lbQueue.poll();
                double dtw = distComputer.dtw(query, poll.getNode().getCentroid(), dtwWindow);
                dtwQueue.add(new QueueElement(poll.getNode(), dtw));
                continue;
            }
            double dtwDistance = dtwQueue.peek().getDistance();
            double lbDistance = lbQueue.peek().getDistance();
            if (lbDistance < dtwDistance) {
                QueueElement poll = lbQueue.poll();
                double dtw = distComputer.dtw(query, poll.getNode().getCentroid(), dtwWindow);
                dtwQueue.add(new QueueElement(poll.getNode(), dtw));
            } else {
                return;
            }
        }
    }

    private boolean searchFinished(){
        /*Search for knn is stopped when queues are empty
        * or when flag stop is set eg. when specified amount of time has passed*/
        if (stop)
            return true;
        if (dtwQueue.isEmpty() && lbQueue.isEmpty())
            return true;
        return false;
    }
    private int findBestChild(TsiTreeNode node) {
        int bestChild = 0;
        ArrayList<TsiTreeNode> children = node.getChildren();
        double bestLb = Double.POSITIVE_INFINITY;
        double bestDtw = Double.POSITIVE_INFINITY;
        TsiTreeNode bestNode = null;
        for (int i=0;i<children.size();i++) {
            TsiTreeNode currentNode = children.get(i);
            double[] centroid = currentNode.getCentroid();
            double lb_keogh = distComputer.lb_keogh(wedge, centroid);
            /* For each node calculate lower_bound if current value isn't
            * the best value yet add node to lbQueue, if it is the best lb value
            * calculate dtw */
            if (lb_keogh < bestLb)  {
                double dtw = distComputer.dtw(query, centroid, dtwWindow);
                /* If current dtw is the best value yet, remember current node as the best
                * and add previous best node to dtwQueue*/
                if (dtw < bestDtw) {
                    if (bestNode != null){
                        dtwQueue.add(new QueueElement(bestNode,bestDtw));
                    }
                    bestDtw = dtw;
                    bestNode = currentNode;
                    bestChild = i;
                    bestLb = lb_keogh;
                } else{
                    dtwQueue.add(new QueueElement(currentNode,dtw));
                }
            } else {
                lbQueue.add(new QueueElement(currentNode,lb_keogh));
            }
        }
        return bestChild;
    }
}
