package si.um.feri.project.map;

import java.util.Date;

public class Event {
    public String _id;
    public Date date;
    public String time;
    public Team home;
    public Team away;
    public String score;
    public String location;
    public Stadium stadium;
    public int season;

    public Event() {
    }

    public Event(String _id, Date date, String time, Team home, Team away, String score, String location, Stadium stadium, int season) {
        this._id = _id;
        this.date = date;
        this.time = time;
        this.home = home;
        this.away = away;
        this.score = score;
        this.location = location;
        this.stadium = stadium;
        this.season = season;
    }
}

