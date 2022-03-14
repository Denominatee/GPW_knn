package gpw.pl.tsi.config;

import gpw.pl.tsi.classifier.trainingset.TrainingSetFactory;
import gpw.pl.tsi.classifier.trainingset.TsiModelProperties;
import gpw.pl.tsi.classifier.tsi.TsiModel;
import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.utils.Interval;
import org.openjdk.jol.info.GraphLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.TimeUnit;

@Configuration
public class GlobalConfiguration {

    @Autowired
    private TrainingSetFactory factory;

    private Logger log = LoggerFactory.getLogger(GlobalConfiguration.class);

    @Bean
    ModelDatabase models() throws SQLException {
        log.info("Preparing model database");
        long timer2 = System.nanoTime();
        ModelDatabase modelMap = new ModelDatabase();
        for (int i = 5; i < 6; i++) {
            int timeSeriesLength = i;
            for (Interval interval: Interval.values()) {
                long timer = System.nanoTime();
                TsiModelProperties properties = new TsiModelProperties(
                        interval,
                        timeSeriesLength,
                        4,
                        10,
                        5,
                        10);
                TimeSeries[] trainingSet = factory.getTrainingSet(properties.getInterval(), properties.getLength());
                ModelKey key = new ModelKey(properties.getLength(), properties.getInterval());
                TsiModel model = new TsiModel(properties);
                model.train(trainingSet);
                modelMap.getMap().put(key,model);
                logTrainingTime(timer,properties);
            }
        }
        logTrainingTime(timer2);
        logDatabaseSize(modelMap);
        return modelMap;
    }

    private void logTrainingTime(long timer) {
        long end = System.nanoTime();
        long elapsedTime = end - timer;
        long convert = TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS);
        log.info("Database created in {} minutes",convert);
    }

    private void logDatabaseSize(ModelDatabase modelMap) {
        long l = GraphLayout.parseInstance(modelMap).totalSize();
        log.info("Database size: " + humanReadableByteCountSI(l));
    }

    private String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private void logTrainingTime(long timer, TsiModelProperties properties) {
        long end = System.nanoTime();
        long elapsedTime = end - timer;
        long convert = TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS);
        int length = properties.getLength();
        Interval interval = properties.getInterval();
        log.info("Model[length={},interval={}] training time was " + convert + " minutes.",length,interval);
    }
}