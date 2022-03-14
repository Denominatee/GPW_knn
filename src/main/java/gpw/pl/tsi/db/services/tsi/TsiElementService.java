package gpw.pl.tsi.db.services.tsi;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.db.DbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.stream.Stream;

@Component
public class TsiElementService {

    @Autowired
    private DbManager dbManager;
    @Value("${spring.datasource.schema}")
    private String schema;
    @Value("${db.element_table}")
    private String table;

    public TimeSeries[] getById(long id) {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        StringBuffer query = new StringBuffer();
        query.append("select * from " + schema + "." + table + " ");
        query.append("where node_id = " + id);
        StringBuffer count = new StringBuffer();
        count.append("select count(*) from " + schema + "." + table + " ");
        count.append("where node_id = " + id);
        TimeSeries[] array = null;
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(count.toString());
            rs.next();
            int size = rs.getInt(1);
            rs = st.executeQuery(query.toString());
            array = new TimeSeries[size];
            int index = 0;
            while (rs.next()){
                String stock = rs.getString("isin");
                Timestamp start = rs.getTimestamp("start");
                Timestamp stop = rs.getTimestamp("stop");
                Array centroidArray = rs.getArray("values");
                Float[] values = (Float[]) centroidArray.getArray();
                double[] unboxed = Stream.of(values).mapToDouble(Float::doubleValue).toArray();
                TimeSeries element = new TimeSeries(unboxed,stock,start,stop);
                array[index++] = element;
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public void save(TimeSeries element, Long id) {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        StringBuffer query = new StringBuffer();
        query.append("insert into " + schema + "." + table + " ");
        query.append("(isin,start,stop,node_id,values) ");
        query.append("values('" + element.getStock() + "', '"  + element.getStart()
                + "', '" + element.getStop() + "', " + id + ", " + element.getValuesString() + ");");
        try {
            st = connection.createStatement();
            int rs = st.executeUpdate(query.toString());
            if (rs == 0){
                throw new SQLException("Creating node failed, no rows affected.");
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
