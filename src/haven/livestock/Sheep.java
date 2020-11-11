package haven.livestock;


import haven.*;

import java.util.HashMap;
import java.util.Map;

public class Sheep extends Animal {
    private int attributesRequired = columns.size() + 1;
    private static final Tex texEwe = Resource.loadtex("gfx/livestockava/ewe");
    private static final Tex texRam = Resource.loadtex("gfx/livestockava/ram");

    public static final Map<String, Column> columns = new HashMap<>(10);

    static {
        int x = 0;
        x = addColumn(columns, "Quality:", "Quality", 0, x);
        x = addColumn(columns, "Breeding quality:", "Breeding", 1, x);
        x = addColumn(columns, "Meat quality:", "Meat", 2, x);
        x = addColumn(columns, "Milk quality:", "Milk", 3, x);
        x = addColumn(columns, "Hide quality:", "Hide", 4, x);
        x = addColumn(columns, "Meat quantity:", "Meat #", 5, x);
        x = addColumn(columns, "Milk quantity:", "Milk #", 6, x);
        x = addColumn(columns, "Wool quality:", "Wool", 7, x);
        x = addColumn(columns, "Wool quantity:", "Wool #", 8, x);
            addColumn(columns, "X", "", 9, x);
    }

    public Sheep(long wndid, String type) {
        super(wndid, type);
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void attributeResolved() {
        attributesRequired--;
    }

    public boolean hasAllAttributes() {
        return attributesRequired == 0;
    }

    public Tex getAvatar() {
        return type.equals("Ewe") ? texEwe : texRam;
    }
}
