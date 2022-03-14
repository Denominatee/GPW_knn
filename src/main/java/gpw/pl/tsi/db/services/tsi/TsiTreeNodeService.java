package gpw.pl.tsi.db.services.tsi;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNode;
import gpw.pl.tsi.db.DbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Stream;

@Component
public class TsiTreeNodeService {

    @Autowired
    private DbManager dbManager;
    @Autowired
    private TsiElementService elementService;
    @Value("${spring.datasource.schema}")
    private String schema;
    @Value("${db.node_table}")
    private String table;

    public Long saveTree(TsiTreeNode tree, Long parent) {
        Long id = saveNode(tree, parent);
        ArrayList<TsiTreeNode> children = tree.getChildren();
        for (TsiTreeNode node : children
        ) {
            saveTree(node, id);
        }
        return id;
    }

    private Long saveNode(TsiTreeNode node, Long parent) {
        if (parent == null)
            //For root
            parent = -1L;
        Connection connection = dbManager.getConnection();
        Statement st = null;
        StringBuffer query = new StringBuffer();
        Long id = null;
        query.append("insert into " + schema + "." + table + " ");
        query.append("(parent,leaf,root,centroid) ");
        query.append("values(" + parent + ", " + node.isLeaf() + ", " + node.isRoot() + ", " + node.getCentoridString() + ") returning id;");
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(query.toString());
            if (rs.next()) {
                id = rs.getLong(1);
            } else {
                throw new SQLException("Creating node failed, no rows affected.");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (node.isLeaf() && id != null) {
            for (TimeSeries element : node.getValues()
            ) {
                elementService.save(element, id);
            }
        }
        return id;
    }

    public TsiTreeNode getById(int id, boolean asRoot) {
        TsiTreeNode tree = new TsiTreeNode();
        if (asRoot)
            tree.setRoot();
        Connection connection = dbManager.getConnection();
        Statement st = null;
        StringBuffer query = new StringBuffer();
        query.append("select * from " + schema + "." + table + " ");
        query.append("where parent = " + id);
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(query.toString());
            while (rs.next()) {
                int nodeId = rs.getInt("id");
                boolean isLeaf = rs.getBoolean("leaf");
                Array centroidArray = rs.getArray("centroid");
                Float[] centroid = (Float[]) centroidArray.getArray();
                if (isLeaf) {
                    TsiTreeNode leaf = new TsiTreeNode();
                    leaf.setLeaf();
                    leaf.setValues(elementService.getById(nodeId));
                    double[] unboxed = Stream.of(centroid).mapToDouble(Float::doubleValue).toArray();
                    leaf.setCentroid(unboxed);
                    tree.addChild(leaf);
                } else {
                    TsiTreeNode node = getById(nodeId, false);
                    double[] unboxed = Stream.of(centroid).mapToDouble(Float::doubleValue).toArray();
                    node.setCentroid(unboxed);
                    tree.addChild(node); //recursive
                }
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tree;
    }
}