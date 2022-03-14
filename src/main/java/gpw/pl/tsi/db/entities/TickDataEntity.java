package gpw.pl.tsi.db.entities;

import java.sql.Timestamp;

public class TickDataEntity {

    private Timestamp date;
    private double price;
    private long volume;

    public TickDataEntity(Timestamp date, double price, long volume) {
        this.date = date;
        this.price = price;
        this.volume = volume;
    }

    public TickDataEntity() {
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}
