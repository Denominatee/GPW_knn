package gpw.pl.tsi.classifier.trainingset;

import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.db.entities.CompanyEntity;
import gpw.pl.tsi.db.entities.TickDataEntity;
import gpw.pl.tsi.db.services.tickdata.CompanyService;
import gpw.pl.tsi.utils.DateUtils;
import gpw.pl.tsi.utils.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

@Component
public class TrainingSetFactory {

    @Autowired
    CompanyService service;

    public TimeSeries[] getTrainingSet(Interval interval, int widthInDays) throws SQLException {
        List<CompanyEntity> all = service.getAll();
        List<TimeSeries> elements = new ArrayList<TimeSeries>();
        int j=0;
        //LocalDateTime dateTime = LocalDateTime.now().minusMonths(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime dateTime = LocalDateTime.now().minusYears(2).minusMonths(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Timestamp startDate = Timestamp.valueOf(dateTime);
        for (CompanyEntity stock: all) {
            List<TimeSeries> timeSeries = getTimeSeries(startDate,stock.getIsin(), interval, widthInDays);
            if (timeSeries != null) {
                elements.addAll(timeSeries);
            }
        }
        return elements.toArray(new TimeSeries[elements.size()]);
    }

    public double[] prepareQuery(String isin, String startDate, String endDate, Interval interval, int length) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.parse(startDate+ " 09:00", formatter));
        Timestamp endTime = Timestamp.valueOf(LocalDateTime.parse(endDate + " 17:00", formatter));
        TickDataEntity tickDataEntity = service.firstEntry(isin,startTime);
        if (tickDataEntity == null)
            return null;
        double previousPrice = tickDataEntity.getPrice();
        double currentPrice = previousPrice;
        Timestamp previousDate = startTime;
        int timeSeriesLength = numOfSteps(interval, length);
        double[] values = new double[++timeSeriesLength];
        ResultSet tickData = service.getTickData(isin, startTime, endTime);
        Timestamp entryDate = previousDate;
        Timestamp currentDate = entryDate;
        int index = 0;
        while (index<timeSeriesLength){
            if (!currentDate.after(entryDate)) {
                previousPrice = currentPrice;
                previousDate = currentDate;
                boolean next = tickData.next();
                if (!next) {
                    //No more tick data, fill rest of cells and break the loop
                    while (index<timeSeriesLength) {
                        values[index++] = previousPrice;
                    }
                    break;
                }
            }
            currentPrice = tickData.getDouble("price");
            currentDate = DateUtils.getNextTickDate(tickData.getString("date"));
            if (!currentDate.before(entryDate)) {
                values[index] = previousPrice;
                entryDate = nextEntryDate(interval,entryDate);
                index++;
            }
        }
        tickData.close();
        return normalize(values);
    }

    public List<TimeSeries> getTimeSeries(Timestamp startDate, String isin, Interval interval, int widthInDays) throws SQLException {
        ArrayList<TimeSeries> list = new ArrayList<>();
        TickDataEntity tickDataEntity = service.firstEntry(isin,startDate);
        if (tickDataEntity == null)
            return null;
        double previousPrice = tickDataEntity.getPrice();
        double currentPrice = previousPrice;
        Timestamp previousDate = startDate;
        int timeSeriesLength = numOfSteps(interval, widthInDays);
        double[] values = new double[++timeSeriesLength];
        ResultSet tickData = service.getTickData(isin,startDate,Timestamp.valueOf(startDate.toLocalDateTime().plusMonths(2)));
        Timestamp entryDate = startDate;
        Timestamp currentDate = entryDate;
        Timestamp timeSeriesStart = startDate;
        int index = 0;
        while (true){
            if (!currentDate.after(entryDate)) {
                previousPrice = currentPrice;
                previousDate = currentDate;
                boolean next = tickData.next();
                if (!next) {
                    //No more tick data, fill rest of cells and break the loop
                    while (index<timeSeriesLength) {
                        values[index++] = previousPrice;
                    }
                    list.add(new TimeSeries(normalize(values),isin,timeSeriesStart, DateUtils.addBusinessDays(timeSeriesStart,widthInDays)));
                    break;
                }
            }
            currentPrice = tickData.getDouble("price");
            currentDate = DateUtils.getNextTickDate(tickData.getString("date"));
            if (!currentDate.before(entryDate)) {
                values[index] = previousPrice;
                entryDate = nextEntryDate(interval,entryDate);
                index++;
                if (index >= timeSeriesLength) {
                    list.add(new TimeSeries(normalize(values),isin,timeSeriesStart, DateUtils.addBusinessDays(timeSeriesStart,widthInDays)));
                    index = resetIndex(index,interval);
                    values = resetValues(values,interval);
                    timeSeriesStart = DateUtils.addBusinessDays(timeSeriesStart,1);
                }
            }
        }
        tickData.close();
        return list;
    }

    private double[] normalize(double[] values2) {
        double[] values = new double[values2.length];
        System.arraycopy(values2, 0, values, 0,values2.length);
        OptionalDouble max = Arrays.stream(values).max();
        OptionalDouble min = Arrays.stream(values).min();
        double diff = max.getAsDouble() - min.getAsDouble();
        for(int i = 0; i < values.length; i++)
        {
            values[i] = (values[i]-min.getAsDouble())/diff;
        }
        return values;
    }

    private Timestamp nextEntryDate(Interval interval, Timestamp entryDate) {
        LocalDateTime dateTime = entryDate.toLocalDateTime();
        LocalDateTime newDate = null;
        switch (interval) {
            case DAY: {
                newDate = dateTime.plusDays(1);
                if (newDate.getDayOfWeek() == DayOfWeek.SATURDAY ){
                    newDate = newDate.plusDays(2);
                }
                break;
            }
            case MINUTES_60: {
                newDate = dateTime.plusHours(1);
                if (newDate.getHour() > 17){
                    if (newDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        newDate = newDate.plusDays(3).withHour(10);
                    } else {
                        newDate = newDate.plusDays(1).withHour(10);
                    }
                }
                break;
            }
            case MINUTES_30: {
                newDate = dateTime.plusMinutes(30);
                if (newDate.getHour() > 17 ||(newDate.getHour() == 17 && newDate.getMinute() > 0)){
                    if (newDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        newDate = newDate.plusDays(3).withHour(9);
                    } else {
                        newDate = newDate.plusDays(1).withHour(9);
                    }
                }
                break;
            }
            case MINUTES_15: {
                newDate = dateTime.plusMinutes(15);
                if (newDate.getHour() > 17||(newDate.getHour() == 17 && newDate.getMinute() > 0)){
                    if (newDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        newDate = newDate.plusDays(3).withHour(9);
                    } else {
                        newDate = newDate.plusDays(1).withHour(9);
                    }
                }
                break;
            }
            case MINUTES_5: {
                newDate = dateTime.plusMinutes(5);
                if (newDate.getHour() > 17||(newDate.getHour() == 17 && newDate.getMinute() > 0)){
                    if (newDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        newDate = newDate.plusDays(3).withHour(9);
                    } else {
                        newDate = newDate.plusDays(1).withHour(9);
                    }
                }
                break;
            }
        }
        return Timestamp.valueOf(newDate);
    }

    private int resetIndex(int index, Interval interval) {
        int position = numOfSteps(interval,1);
        return index - position;
    }

    private double[] resetValues(double[] values, Interval interval) {
        int position = numOfSteps(interval,1);
        int size = values.length;
        double[] newValues = new double[size];
        System.arraycopy(values, position, newValues, 0,size-position);
        return  newValues;
    }

    private int numOfSteps(Interval interval, int widthInDays) {
        int minutes = interval.getMinutes();
        int i = 480 * widthInDays;
        return i/minutes;
    }
}
