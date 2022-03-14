package gpw.pl.tsi.config;

import gpw.pl.tsi.classifier.tsi.TsiModel;

import java.util.HashMap;
import java.util.Map;

public class ModelDatabase {

    Map<ModelKey,TsiModel> map = new HashMap<ModelKey,TsiModel>();

    public Map<ModelKey, TsiModel> getMap() {
        return map;
    }
}
