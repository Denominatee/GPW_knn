package gpw.pl.tsi.db.services.tickdata;

import gpw.pl.tsi.db.DbManager;
import gpw.pl.tsi.db.entities.CompanyEntity;
import gpw.pl.tsi.db.entities.TickDataEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyService {

    @Autowired
    private DbManager dbManager;

    public List<CompanyEntity> getAll() {
        List<CompanyEntity> stocks = new ArrayList<>();
        Connection connection = dbManager.getConnection();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("SELECT * FROM gpw_data.company_dict");
            while (rs.next()) {
                String isin = rs.getString("isin");
                String company_name = rs.getString("company_name");
                stocks.add(new CompanyEntity(isin,company_name));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    public TickDataEntity firstEntry(String stock) {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        ResultSet rs = null;
        Double price = null;
        Timestamp date = null;
        Integer volume = null;
        TickDataEntity tick = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("select price, date, volume from gpw_data.tick_data a where isin = '" + stock + "' order by date asc limit 1");
            if (rs.next()) {
                tick = new TickDataEntity();
                price = rs.getDouble(1);
                date = rs.getTimestamp(2);
                volume = rs.getInt(3);
                tick.setDate(date);
                tick.setVolume(volume);
                tick.setPrice(price);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tick;
    }

    public Timestamp getFirstDate(String isin) {
        Connection connection = dbManager.getConnection();
        Timestamp date = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("SELECT * FROM gpw_data.tick_data WHERE isin = '" + isin + "' order by date asc limit 1");
            rs.next();
            date = rs.getTimestamp("date");
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return date;
    }

    public  ResultSet getTickData(String stock, Timestamp start, Timestamp stop) throws SQLException {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        ResultSet rs = null;
        StringBuffer sb = new StringBuffer();
        sb.append("(SELECT * FROM gpw_data.tick_data WHERE date between '" + start + "' and '" + stop + "' and isin = '" + stock + "')");
        sb.append("UNION ALL");
        sb.append("(select * from gpw_data.tick_data WHERE date > '" + stop + "' and isin = '" + stock + "' order by date asc limit 1) order by date asc");
        st = connection.createStatement();
        rs = st.executeQuery(sb.toString());
        return rs;
    }

    public  ResultSet getAllTickData(String stock) throws SQLException {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        ResultSet rs = null;
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM gpw_data.tick_data WHERE isin = '" + stock + "' order by date asc");
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public TickDataEntity firstEntry(String isin, Timestamp startDate) {
        Connection connection = dbManager.getConnection();
        Statement st = null;
        ResultSet rs = null;
        Double price = null;
        Timestamp date = null;
        Integer volume = null;
        TickDataEntity tick = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery("select price, date, volume from gpw_data.tick_data " +
                    "a where isin = '" + isin + "' and date < '" + startDate + "'order by date desc limit 1");
            if (rs.next()) {
                tick = new TickDataEntity();
                price = rs.getDouble(1);
                date = rs.getTimestamp(2);
                volume = rs.getInt(3);
                tick.setDate(date);
                tick.setVolume(volume);
                tick.setPrice(price);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tick;
    }
}
