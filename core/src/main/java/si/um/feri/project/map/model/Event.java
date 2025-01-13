package si.um.feri.project.map.model;

import java.util.List;

import si.um.feri.project.map.utils.Geolocation;

public class Event {
    public String _id;
    public String name;
    public String description;
    public String activity;
    public String date;
    public String time;
    public String image;
    public int predicted_count;
    public Host host;
    public List<String> followers;
    public Geolocation location;

    public Event() {
    }
}

