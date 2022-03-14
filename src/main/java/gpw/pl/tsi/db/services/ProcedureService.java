package gpw.pl.tsi.db.services;

import gpw.pl.tsi.db.DbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class ProcedureService {

    @Autowired
    private DbManager dbManager;
    @Value("${spring.datasource.schema}")
    private String schema;

    public void clearHistoricalModels() {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        StringBuffer query = new StringBuffer();
        query.append("SELECT " + schema + ".clear_historical_models()");
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(query.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}