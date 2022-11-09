package nsu.maxwell.parser;

import com.google.gson.Gson;
import nsu.maxwell.json.location.HintItems;
import nsu.maxwell.json.location.Info;

import java.util.ArrayList;

public class LocationParser {
    public static ArrayList<Info> parse(String srcJson) {
        Gson gson = new Gson();
        HintItems items = gson.fromJson(srcJson, HintItems.class);
        return items.hits();
    }
}
