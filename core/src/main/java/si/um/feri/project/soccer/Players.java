package si.um.feri.project.soccer;

public enum Players {
    PLAYER1("bravop"),
    PLAYER2("celjep"),
    PLAYER3("domžalep"),
    PLAYER4("koperp"),
    PLAYER5("mariborp"),
    PLAYER6("murap"),
    PLAYER7("nafta 1903p"),
    PLAYER8("olimpijap"),
    PLAYER9("primorjep"),
    PLAYER10("radomljep");

    private String name;

    Players(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
