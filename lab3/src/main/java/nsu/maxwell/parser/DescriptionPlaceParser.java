package nsu.maxwell.parser;

import com.google.gson.Gson;
import nsu.maxwell.json.interestingplaces.DescriptionPlace;

public class DescriptionPlaceParser {
    public static DescriptionPlace parse(String srcJson) {
        Gson gson = new Gson();
        return gson.fromJson(srcJson, DescriptionPlace.class);
    }
}
