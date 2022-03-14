package gpw.pl.tsi.db.services.tsi;

import gpw.pl.tsi.classifier.trainingset.TsiModelProperties;
import gpw.pl.tsi.classifier.tsi.TsiModel;
import gpw.pl.tsi.classifier.tsi.datastructure.tree.TsiTreeNode;
import gpw.pl.tsi.db.DbManager;
import gpw.pl.tsi.utils.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class TsiModelService {

    @Autowired
    private DbManager dbManager;
    @Autowired
    private TsiTreeNodeService treeNodeService;
    @Value("${spring.datasource.schema}")
    private String schema;
    @Value("${db.model_table}")
    private String table;

    public Long save(TsiModel model){
        Connection connection = dbManager.getConnection();
        Long modelId = null;
        TsiTreeNode root = model.getTree();
        System.out.println("saving tree");
        Long rootId = treeNodeService.saveTree(root, null);
        System.out.println("tree saved");
        Statement st = null;
        StringBuffer query = new StringBuffer();
        query.append("insert into " + schema + "." + table + " ");
        query.append("(root_id,no_clusters,dtw_window,cluster_size,no_means,interval,length) ");
        query.append("values(" + rootId + ", " + model.getNumNodeClusters() + ", " + model.getDtwWindow()
                + ", " + model.getClusterCapacity() + ", " + model.getNumMeans() + ", '"
                + model.getInterval() + "', " + model.getLength() + ") returning id;");
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(query.toString());
            if (rs.next()){
                modelId = rs.getLong(1);
            } else {
                throw new SQLException("Creating node failed, no rows affected.");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelId;
    }

    public TsiModel getByLengthAndInterval(Interval interval, int timeSeriesLength) {
        Connection connection = dbManager.getConnection();
        StringBuffer query = new StringBuffer();
        query.append("select * from " + schema + "." + table + " ");
        query.append("where length = " + timeSeriesLength);
        query.append(" and interval = '"+ interval + "';");
        Statement st = null;
        TsiModel model = null;
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(query.toString());
            if (rs.next()){
                if (rs.isLast()){
                    int root_id = rs.getInt("root_id");
                    int no_means = rs.getInt("no_means");
                    int no_clusters = rs.getInt("no_clusters");
                    int dtw_window = rs.getInt("dtw_window");
                    int cluster_size = rs.getInt("cluster_size");
                    TsiModelProperties properties = new TsiModelProperties(
                            interval,timeSeriesLength,no_means,
                            no_clusters,dtw_window,cluster_size);
                    model = new TsiModel(properties);
                    TsiTreeNode tree = treeNodeService.getById(root_id,true);
                    model.setTree(tree);
                } else {
                    throw new SQLException("Non unique result.");
                }
            } else {
                throw new SQLException("Not found, no rows affected.");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }
}