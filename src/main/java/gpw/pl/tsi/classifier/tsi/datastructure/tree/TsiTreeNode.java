package gpw.pl.tsi.classifier.tsi.datastructure.tree;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;

import java.util.ArrayList;

public class TsiTreeNode {

    private boolean isRoot = false;
    private boolean isLeaf = false;
    private TimeSeries[] values;
    private double[] centroid;

    private ArrayList<TsiTreeNode> children = new ArrayList<>();

    public void addChild(TsiTreeNode node){
        this.children.add(node);
    }

    public void setRoot() {
        this.isRoot = true;
    }

    public void setLeaf() {
        isLeaf = true;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public ArrayList<TsiTreeNode> getChildren() {
        return children;
    }

    public TimeSeries[] getValues() {
        return values;
    }

    public void setValues(TimeSeries[] values) {
        this.values = values;
    }

    public double[] getCentroid() {
        return centroid;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    public int getSize() {
        int sum = 0;
        if (this.isLeaf == true){
            return values.length;
        } else {
            ArrayList<TsiTreeNode> children = this.children;
            for (TsiTreeNode child:children
                 ) {
                sum = sum + child.getSize();
            }
        }
        return sum;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public String getCentoridString(){
        if (centroid == null)
                return null;
        StringBuffer sb = new StringBuffer("");
        sb.append("\'{");
        for (int i=0;i<centroid.length;i++) {
            sb.append(centroid[i]);
            if (i + 1 < centroid.length)
                sb.append(", ");
        }
        sb.append("}\'");
        return sb.toString();
    }
}
