package si.um.feri.project.map;

public class Team {
    public String _id;
    public String name;
    public String logoPath;

    public Team() {
    }

    public Team(String _id, String name, String logoPath) {
        this._id = _id;
        this.name = name;
        this.logoPath = logoPath;
    }
}
