package nsu.maxwell.parser;

import com.google.gson.Gson;
import nsu.maxwell.json.weather.WeatherInfo;

public class WeatherParser {
    public WeatherInfo parse(String srcJson) {
        Gson gson = new Gson();
        return gson.fromJson(srcJson, WeatherInfo.class);
    }
}
