package gpw.pl.tsi.config;

import gpw.pl.tsi.utils.Interval;

import java.util.Objects;

public class ModelKey {

    private int length;
    private Interval interval;
    private int hashCode;

    public ModelKey(int length, Interval interval) {
        this.length = length;
        this.interval = interval;
        this.hashCode = Objects.hash(length, interval);
    }
    public int getLength() {
        return length;
    }
    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ModelKey that = (ModelKey) o;
        return length == that.length && interval == that.interval;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
