package si.um.feri.project.map.model;

public class TeamRecord {
    public String _id;
    public String name;
    public String logoPath;

    public TeamRecord() {
    }

    public TeamRecord(String _id, String name, String logoPath) {
        this._id = _id;
        this.name = name;
        this.logoPath = logoPath;
    }
}
