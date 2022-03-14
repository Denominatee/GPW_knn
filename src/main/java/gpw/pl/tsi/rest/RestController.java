package gpw.pl.tsi.rest;

import gpw.pl.tsi.classifier.trainingset.TrainingSetFactory;
import gpw.pl.tsi.classifier.trainingset.TsiModelProperties;
import gpw.pl.tsi.classifier.tsi.TsiModel;
import gpw.pl.tsi.classifier.tsi.datastructure.cluster.TimeSeries;
import gpw.pl.tsi.db.services.ProcedureService;
import gpw.pl.tsi.db.services.tsi.TsiModelService;
import gpw.pl.tsi.utils.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.SQLException;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    TsiModelService modelService;
    @Autowired
    ProcedureService procedureService;
    @Autowired
    private TrainingSetFactory factory;
    private String datePattern = "yyyy-MM-dd'T'HH:mm";

    @PostMapping("/build")
    void build() throws SQLException {
        TsiModelProperties properties = new TsiModelProperties(Interval.DAY,5,4,
                10,5,10);
        TimeSeries[] trainingSet = factory.getTrainingSet(properties.getInterval(), properties.getLength());
        TsiModel model = new TsiModel(properties);
        model.train(trainingSet);
        Long save = modelService.save(model);
        System.out.println(save);
    }

    @GetMapping("/load")
    void load() throws SQLException {
        System.out.println("loading model");
        long startTime = System.nanoTime();
        TsiModel model = modelService.getByLengthAndInterval(Interval.DAY, 5);
        System.out.println("Loading time " + (System.nanoTime() - startTime) / 1000000000.0);
        System.out.println("loaded model");
        //model.knn()
    }

    @PostMapping("/clear")
    void clear() throws SQLException {
        procedureService.clearHistoricalModels();
    }
}
