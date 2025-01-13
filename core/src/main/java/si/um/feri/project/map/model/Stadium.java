package si.um.feri.project.map.model;

import si.um.feri.project.map.utils.Geolocation;

public class Stadium {
    public String _id;
    public String name;
    public String teamId;
    public int season;
    public Geolocation location;

    public Stadium() {
    }

    public Stadium(String _id, String name, String teamId, int season, Geolocation location) {
        this._id = _id;
        this.name = name;
        this.teamId = teamId;
        this.season = season;
        this.location = location;
    }
}
