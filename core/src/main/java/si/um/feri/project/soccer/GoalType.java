package si.um.feri.project.soccer;

import java.util.HashMap;
import java.util.Map;

public enum GoalType {
    SMALL(0,RegionNames.Textures.SMALL),NORMAL(1,RegionNames.Textures.NORMAL),BIG(2,RegionNames.Textures.BIG);
    private int value;
    private String regionName;
    private static Map map = new HashMap<>();

    static {
        for (GoalType pageType : GoalType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static GoalType valueOf(int goalType) {
        return (GoalType) map.get(goalType);
    }
    private GoalType(int value,String regionName) {
        this.value = value;
        this.regionName = regionName;
    }
    public int getValue() {
        return value;
    }
    public String  getRegionname() {
        return regionName;
    }
}
