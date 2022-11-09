package nsu.maxwell.parser;

import com.google.gson.Gson;
import nsu.maxwell.json.interestingplaces.Feature;
import nsu.maxwell.json.interestingplaces.InterestingPlace;

import java.util.ArrayList;

public class PlaceParser {

    public static ArrayList<Feature> parse(String srcJson) {
        Gson gson = new Gson();
        InterestingPlace places = gson.fromJson(srcJson, InterestingPlace.class);
        return places.features();
    }
}
