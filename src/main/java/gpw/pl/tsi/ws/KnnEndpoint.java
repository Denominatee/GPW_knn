package gpw.pl.tsi.ws;

import gpw.pl.tsi.classifier.trainingset.TrainingSetFactory;
import gpw.pl.tsi.classifier.tsi.TsiModel;
import gpw.pl.tsi.classifier.tsi.datastructure.TimeSeriesResult;
import gpw.pl.tsi.config.ModelDatabase;
import gpw.pl.tsi.config.ModelKey;
import gpw.pl.tsi.db.services.tsi.TsiModelService;
import gpw.pl.tsi.utils.JaxbUtils;
import gpw.pl.tsi.ws.definition.Interval;
import gpw.pl.tsi.ws.definition.KnnRequest;
import gpw.pl.tsi.ws.definition.KnnResponse;
import gpw.pl.tsi.ws.definition.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


@Endpoint
public class KnnEndpoint {

    private static final String NAMESPACE_URI = "http://gpw.pl/tsi";
    private Logger logger = LoggerFactory.getLogger(KnnEndpoint.class);

    @Autowired
    private TsiModelService modelService;
    @Autowired
    JaxbUtils xmlUtils;
    @Autowired
    ModelDatabase modelMap;
    @Autowired
    TrainingSetFactory factory;



    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "knnRequest")
    @ResponsePayload
    public KnnResponse handleKnnRequest(@RequestPayload KnnRequest request) throws Exception {
        logger.info(System.lineSeparator() + "KnnRequest xml:" + xmlUtils.getAsString(request));
        gpw.pl.tsi.utils.Interval interval = convertInterval(request.getInterval());
        BigInteger length = request.getLength();
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();
        BigInteger k = request.getK();
        String isin = request.getIsin();
        double[] query = factory.prepareQuery(isin,startDate,endDate,interval,length.intValue());
        ModelKey key = new ModelKey(5, gpw.pl.tsi.utils.Interval.MINUTES_30);
        TsiModel tsiModel = modelMap.getMap().get(key);
        TimeSeriesResult[] knn = tsiModel.knn(query, k.intValue());
        //*Preparing response for the request*//*
        KnnResponse response = new KnnResponse();
        response.getResult().addAll(convert(knn));
        logger.info(System.lineSeparator() + "KnnResponse xml:" + xmlUtils.getAsString(response));
        return response;
    }

    private List<TimeSeries> convert(TimeSeriesResult[] ts) {
        List<TimeSeries> list = Arrays.stream(ts)
                .map(p -> toTs(p)).collect(Collectors.toList());
        return list;
    }

    private TimeSeries toTs(TimeSeriesResult ts){
        if (ts == null)
                return null;
        TimeSeries timeSeries = new gpw.pl.tsi.ws.definition.TimeSeries();
        timeSeries.setDistance(ts.getDistance());
        timeSeries.setIsin(ts.getLabel());
        List<Double> collect = DoubleStream.of(ts.getData()).boxed().collect(Collectors.toList());
        timeSeries.getValues().addAll(collect);
        timeSeries.setStartDate(ts.getStart().toString());
        timeSeries.setEndDate(ts.getStop().toString());
        return timeSeries;
    }

    private gpw.pl.tsi.utils.Interval convertInterval(Interval wsInterval) {
        switch (wsInterval) {
            case MINUTES_5 -> {
                return gpw.pl.tsi.utils.Interval.MINUTES_5;
            }
            case MINUTES_15 -> {
                return gpw.pl.tsi.utils.Interval.MINUTES_15;
            }
            case MINUTES_30 -> {
                return gpw.pl.tsi.utils.Interval.MINUTES_30;
            }
            case MINUTES_60 -> {
                return gpw.pl.tsi.utils.Interval.MINUTES_60;
            }
            case DAY -> {
                return gpw.pl.tsi.utils.Interval.DAY;
            }
        }
        return null;
    }
}