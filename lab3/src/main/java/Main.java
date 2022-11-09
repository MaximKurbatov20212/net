import nsu.maxwell.json.interestingplaces.DescriptionPlace;
import nsu.maxwell.json.interestingplaces.Feature;
import nsu.maxwell.json.location.Info;
import nsu.maxwell.json.location.Point;
import nsu.maxwell.json.weather.WeatherInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static Client client = new Client();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Input location: ");
        String location = scanner.nextLine();

        ArrayList<Info> locations = client.getLocations(location).get();
        if (locations.size() == 0) {
            System.out.println("No locations, sorry");
            return;
        }

        printLocations(locations);

        int number = getNumberOfLocation(scanner, locations.size());

        System.out.println(locations.get(number - 1).name());
        Point point = locations.get(number - 1).point;

        CompletableFuture<Information> info = getInfo(point, client);
        printInformationAboutPlaces(info.join());
    }

    // if not int -> number = 1
    private static int getNumberOfLocation(Scanner scanner, int size) {
        try {
            while (true) {
                System.out.println("Input number: ");
                int number = scanner.nextInt();
                if (number > size || number <= 0) {
                    System.out.println("Invalid number");
                } else return number;
            }
        }
        //
        catch (Throwable ignore) {
            System.out.println("Invalid value");
            return 1;
        }
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

    public static CompletableFuture<Information> getInfo(Point point, Client client) {

        CompletableFuture<WeatherInfo> weatherInfoCompletableFuture = client.getWeather(point);

        CompletableFuture<List<DescriptionPlace>> descriptions =
                client.getInterestingPlaces(point).thenCompose(features -> myAllOf(features.stream()
                    .map(Feature::id)
                    .map(client::getDetailsAboutInterestingPlaces).toList()));

        CompletableFuture<Void> all = CompletableFuture.allOf(descriptions, weatherInfoCompletableFuture);

        return all.thenApply(x -> new Information(descriptions.join(), weatherInfoCompletableFuture.join()));
    }

    // List<CompletableFuture<T>> -> CompletableFuture<List<T>>
    public static <T> CompletableFuture<List<T>> myAllOf (List<CompletableFuture<T>> futures) {
        return futures.stream()
                .collect(collectingAndThen(toList(), l -> CompletableFuture.allOf(l.toArray(new CompletableFuture[0]))
                                .thenApply(__ -> l.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList()))));
    }

    private static void printLocations(ArrayList<Info> infos) {
        int i = 0;
        for (Info info : infos) {
            System.out.println((++i) + ") " + info.name());
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
}