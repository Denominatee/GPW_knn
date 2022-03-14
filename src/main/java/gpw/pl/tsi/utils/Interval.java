package gpw.pl.tsi.utils;

public enum Interval {

    MINUTES_5(300,5),
    MINUTES_15(900,15),
    MINUTES_30(1800,30),
    MINUTES_60(3600,60),
    DAY(28800,480);

    private int seconds;
    private int minutes;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    Interval(int seconds, int minutes) {
        this.seconds = seconds;
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }
}
