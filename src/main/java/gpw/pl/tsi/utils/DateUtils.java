package gpw.pl.tsi.utils;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static LocalDateTime getStartDate(Timestamp start){
        LocalDateTime date = start.toLocalDateTime();
        if (isAfterFriday5pm(date)){
            return getNextMonday9am(date);
        }
        if (isAfter5pm(date))
            return getNextDay9am(date);
        if (isBefore8am(date))
            return date.withHour(9).withMinute(0).withSecond(0);
        return date.withSecond(0);
    }

    public static Timestamp addMinutes(Timestamp ts, int min) {
        return Timestamp.valueOf(ts.toLocalDateTime().plusMinutes(min));
    }

    public static Timestamp subtractMinutes(Timestamp ts, int min) {
        return Timestamp.valueOf(ts.toLocalDateTime().minusMinutes(min));
    }

    public static Timestamp addBusinessMinutes(Timestamp ts, int min) {
        LocalDateTime newDate = ts.toLocalDateTime().plusMinutes(min);
        if (isAfterHour(newDate,17))
            newDate = newDate.plusDays(1).minusHours(8);
        DayOfWeek dayOfWeek = newDate.getDayOfWeek();
        if (DayOfWeek.SUNDAY == dayOfWeek)
            newDate = newDate.plusDays(1);
        if (DayOfWeek.SATURDAY == dayOfWeek)
            newDate = newDate.plusDays(2);
        return Timestamp.valueOf(newDate);
    }

    public static Timestamp subtractBusinessMinutes(Timestamp ts, int min) {
        LocalDateTime newDate = ts.toLocalDateTime().minusMinutes(min);
        if (isBeforeHour(newDate,9)) {
            newDate = newDate.minusDays(1).plusHours(8);
        }
        DayOfWeek dayOfWeek = newDate.getDayOfWeek();
        if (DayOfWeek.SUNDAY == dayOfWeek)
            newDate = newDate.minusDays(2);
        if (DayOfWeek.SATURDAY == dayOfWeek)
            newDate = newDate.minusDays(1);
        return Timestamp.valueOf(newDate);
    }

    public static LocalDateTime getEndDate(Timestamp end) {
        LocalDateTime date = end.toLocalDateTime();
        if (DateUtils.isAfterFriday5pm(date)){
            return getPreviousFriday5pm(date);
        }
        if (isAfter5pm(date))
            return date.withHour(17).withMinute(0).withSecond(0);
        if (isBefore8am(date))
            return getPreviousDay5pm(date);
        return date.withSecond(0);
    }

    private static LocalDateTime getPreviousDay5pm(LocalDateTime date) {
        return date.minusDays(1).withHour(17).withMinute(0).withSecond(0);
    }

    private static LocalDateTime getNextDay9am(LocalDateTime date) {
        return date.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    private static boolean isAfterFriday5pm(LocalDateTime date){
        int value = date.getDayOfWeek().getValue();
        if (value > 5)
            return true;
        if (value == 5 && isAfter5pm(date))
            return true;
        if (value == 1 && isBefore8am(date))
            return true;
        return false;
    }

    private static boolean isAfter5pm(LocalDateTime date){
        int hour = date.getHour();
        if (hour>=17)
            return true;
        return false;
    }

    private static boolean isBefore8am(LocalDateTime date){
        int hour = date.getHour();
        if (hour<=8)
            return true;
        return false;
    }

    private static boolean isBeforeHour(LocalDateTime date, int hour){
        int h = date.getHour();
        if (h<hour)
            return true;
        return false;
    }

    private static boolean isAfterHour(LocalDateTime date, int hour){
        int h = date.getHour();
        if (h>hour || (h==hour & date.getMinute() > 0))
            return true;
        return false;
    }

    private static LocalDateTime getNextMonday9am(LocalDateTime date){
        int value = date.getDayOfWeek().getValue();
        LocalDateTime monday9am;
        if (value == 1) {
            monday9am = date.withHour(9).withMinute(0).withSecond(0).withNano(0);
        } else {
            monday9am = date.plusDays(8 - value).withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
        return monday9am;
    }

    private static LocalDateTime getPreviousFriday5pm(LocalDateTime date) {
        int value = date.getDayOfWeek().getValue();
        LocalDateTime friday5pm;
        if (value < 5) {
            friday5pm = date.minusDays(2+value).withHour(17).withMinute(0).withSecond(0).withNano(0);
        } else {
            friday5pm = date.minusDays(value - 5).withHour(17).withMinute(0).withSecond(0).withNano(0);
        }
        return friday5pm;
    }

    public static int getNumOfSteps(Timestamp start, Timestamp end, int interval) {
        //Search for first and last valid (market is open) date
        LocalDateTime startDate = getStartDate(start);
        LocalDateTime endDate = getEndDate(end);

        //initial number of steps (amount of minutes between start and end)
        Long minutes = ChronoUnit.MINUTES.between(startDate, endDate);
        /*Decrease number of steps for time when stock market is closed*/
        long dayspassed = ChronoUnit.DAYS.between(startDate.withHour(0).withMinute(0), endDate.withHour(0).withMinute(0));
        int dayOfWeek = startDate.getDayOfWeek().getValue();
        for (int i=0;i<dayspassed;i++){
            if (dayOfWeek == 5){
                minutes = minutes - 3839;   //3839 is a number of minutes between friday 17:00 and monday 9:00
                i = i + 2;
                dayOfWeek = addToDayOfTheWeek(dayOfWeek, 3);
            } else {
                minutes = minutes - 959;
                dayOfWeek = addToDayOfTheWeek(dayOfWeek, 1);
            }
        }
        Long noSteps = minutes / interval;
        noSteps++;
        return noSteps.intValue();
    }

    private static int addToDayOfTheWeek(int dayOfTheweek, int increment) {
        return (dayOfTheweek + increment) % 7;
    }


    public static Timestamp getNextTickDate(String date) {
        Timestamp timestamp = Timestamp.valueOf(date);
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        return Timestamp.valueOf(localDateTime);
    }

    public static Timestamp addMinute(Timestamp start) {
        LocalDateTime date = start.toLocalDateTime().plusMinutes(1);
        if (isAfterFriday5pm(date))
            return Timestamp.valueOf(getNextMonday9am(date));
        if (isAfter5pm(date))
            return Timestamp.valueOf(getNextDay9am(date));
        return Timestamp.valueOf(date);
    }

    public static Timestamp addBusinessDays(Timestamp timeSeriesStart, int businessDays) {
        int weeks  = businessDays/5;
        int rest = businessDays - (5 * weeks);
        LocalDateTime newDate = timeSeriesStart.toLocalDateTime().plusDays(7 * weeks);
        DayOfWeek dayOfWeek = newDate.getDayOfWeek();
        int value = dayOfWeek.getValue();
        if ((value + rest) > 5) {
            newDate = newDate.plusDays(rest + 2);
        } else {
            newDate = newDate.plusDays(rest);
        }
        return Timestamp.valueOf(newDate);
    }
}
