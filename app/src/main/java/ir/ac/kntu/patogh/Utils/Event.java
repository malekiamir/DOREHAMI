package ir.ac.kntu.patogh.Utils;

public class Event {
    private String name;
    private String desc;
    private String date;
    private String capacity;
    private String id;

    public Event() {
    }

    public Event(String name, String desc, String date, String capacity, String id) {
        this.name = name;
        this.desc = desc;
        this.date = date;
        this.capacity = capacity;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}
