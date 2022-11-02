import nsu.maxwell.json.interestingplaces.DescriptionPlace;
import nsu.maxwell.json.interestingplaces.Feature;
import nsu.maxwell.json.location.Info;
import nsu.maxwell.json.location.Point;
import nsu.maxwell.json.weather.WeatherInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static Client client = new Client();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String location = scanner.nextLine();
        ArrayList<Info> locations = client.getLocations(location).get();
        printLocations(locations);

        int number = scanner.nextInt();
        System.out.println(locations.get(number - 1).name());
        Point point = locations.get(number - 1).point;

        Information info = getInfo(point, client);
        printInformationAboutPlaces(info);
    }

    private static void printInformationAboutPlaces(Information info) {
        System.out.println("WeatherInfo: \n" + info.weatherInfo + "\n");
        System.out.println("Interesting places: ");
        for (DescriptionPlace descriptionPlace : info.descriptionPlaces) {
            if (!Objects.equals(descriptionPlace.name(), "") && descriptionPlace.name() != null) {
                System.out.print(descriptionPlace);
            }
        }
    }

    static class Information {
        List<DescriptionPlace> descriptionPlaces;
        WeatherInfo weatherInfo;

        public Information(List<DescriptionPlace> descriptionPlaces, WeatherInfo weatherInfo) {
            this.descriptionPlaces = descriptionPlaces;
            this.weatherInfo = weatherInfo;
        }
    }

    public static Information getInfo(Point point, Client client) {
        CompletableFuture<WeatherInfo> weatherInfoCompletableFuture = client.getWeather(point);

        List<CompletableFuture<DescriptionPlace>> descriptions = client.getInterestingPlaces(point).
                thenComposeAsync(features -> CompletableFuture.completedFuture(features.stream()
                    .map(Feature::id)
                    .map(client::getDetailsAboutInterestingPlaces)
                    .toList())).join();

        return new Information(descriptions.stream().map(CompletableFuture::join).toList(), weatherInfoCompletableFuture.join());
    }

    private static void printLocations(ArrayList<Info> infos) {
        int i = 0;
        for (Info info : infos) {
            System.out.println((++i) + ") " + info.name());
        }
    }
}