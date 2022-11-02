package nsu.maxwell.json.interestingplaces;

public record DescriptionPlace(String name, Wiki wikipedia_extracts) {
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Name: ").append(name).append("\n");

        if (wikipedia_extracts == null) {
            stringBuilder.append("No description\n");
            return stringBuilder.toString();
        }

        return stringBuilder.append(wikipedia_extracts).append("\n").toString();
    }
}
record Wiki(String title, String text) {
    @Override
    public String toString() {
        return "Title: " + title + "\n" + "About: " + text + "\n";
    }
}