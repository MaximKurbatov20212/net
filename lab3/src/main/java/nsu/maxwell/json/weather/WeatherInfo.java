package nsu.maxwell.json.weather;

public record WeatherInfo(Double visibility, Wind wind) {
    @Override
    public String toString() {
        return "Visibility: " + visibility.toString() + "\n" + "Wind speed: " + wind.speed().toString();
    }
}
