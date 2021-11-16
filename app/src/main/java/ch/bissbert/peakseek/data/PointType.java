package ch.bissbert.peakseek.data;

public class PointType {
    private int id;
    private String name;

    public PointType() {
    }

    public PointType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
