package nsu.maxwell.json.location;

import java.util.Objects;

public final class Info {
    public final Point point;
    private final String name;

    Info(Point point, String name) {
        this.point = point;
        this.name = name;
    }

    public Point point() {
        return point;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Info) obj;
        return Objects.equals(this.point, that.point) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, name);
    }

    @Override
    public String toString() {
        return "Info[" +
                "point=" + point + ", " +
                "name=" + name + ']';
    }
}
