package si.um.feri.project.map.model;

import java.util.Date;

public class Match {
    public String _id;
    public Date date;
    public String time;
    public TeamRecord home;
    public TeamRecord away;
    public String score;
    public String location;
    public Stadium stadium;
    public int season;

    public Match() {
    }

    public Match(String _id, Date date, String time, TeamRecord home, TeamRecord away, String score, String location, Stadium stadium, int season) {
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

