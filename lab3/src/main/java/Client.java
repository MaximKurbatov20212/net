import nsu.maxwell.json.interestingplaces.DescriptionPlace;
import nsu.maxwell.json.interestingplaces.Feature;
import nsu.maxwell.json.interestingplaces.InterestingPlace;
import nsu.maxwell.json.location.*;
import nsu.maxwell.json.weather.WeatherInfo;
import nsu.maxwell.parser.DescriptionPlaceParser;
import nsu.maxwell.parser.LocationParser;
import nsu.maxwell.parser.PlaceParser;
import nsu.maxwell.parser.WeatherParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Client {
    CompletableFuture<ArrayList<Info>> getLocations(String location) {
        LocationParser locationParser = new LocationParser();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://graphhopper.com/api/1/geocode?q=" + location + "&locale=en&key=e4b3a63b-9c66-4b66-8141-80f404ff6aff"))
                .GET() // GET is default
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(locationParser::parse);
    }

    CompletableFuture<WeatherInfo> getWeather(Point point) {
        WeatherParser weatherParser = new WeatherParser();
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + point.lat() + "&lon=" + point.lng() + "&appid=6f4ca3aebf5dcab1456e040a09976128";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // GET is default
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApplyAsync(HttpResponse::body)
                        .thenApplyAsync(weatherParser::parse);
    }
// TODO: check status code after .get()
    CompletableFuture<ArrayList<Feature>> getInterestingPlaces(Point point) {
        double eps = 0.02;
        String url = "http://api.opentripmap.com/0.1/ru/places/bbox?lon_min=" + (point.lng() - eps) + "&lat_min=" + (point.lat() - eps) + "&lon_max=" + (point.lng() + eps) + "&lat_max=" + (point.lat() + eps) + "&format=geojson&apikey=5ae2e3f221c38a28845f05b69e17e65d313b700e9e012a8d4a35642e";

        System.out.println(url);

        PlaceParser placeParser = new PlaceParser();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // GET is default
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(placeParser::parse);
    }

    CompletableFuture<DescriptionPlace> getDetailsAboutInterestingPlaces(String id) {
        String url = "http://api.opentripmap.com/0.1/ru/places/xid/" + id + "?apikey=5ae2e3f221c38a28845f05b69e17e65d313b700e9e012a8d4a35642e";

        DescriptionPlaceParser descriptionPlaceParser = new DescriptionPlaceParser();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // GET is default
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(descriptionPlaceParser::parse);
    }
}
