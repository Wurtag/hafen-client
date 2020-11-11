package haven;


import java.util.*;

public class InventoryBelt extends Widget implements DTarget {
    private static final Tex invsq = Resource.loadtex("gfx/hud/invsq-opaque");
    private static final Coord sqsz = new Coord(36, 33);
    public boolean dropul = true;
    public Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();

    @RName("inv-belt")
    public static class $_ implements Factory {
        public Widget create(UI ui, Object[] args) {
            return new InventoryBelt((Coord) args[0]);
        }
    }

    public void draw(GOut g) {
        Coord c = new Coord();
        for (; c.x < isz.x * isz.y * sqsz.x; c.x += sqsz.x)
            g.image(invsq, c);
        super.draw(g);
    }

    public InventoryBelt(Coord sz) {
        super(invsq.sz().add(new Coord(-1 + 3, -1)).mul(sz.x * sz.y, 1).add(new Coord(1, 1)));
        isz = sz;
    }

    @Override
    public boolean mousewheel(Coord c, int amount) {
        return false;
    }

    @Override
    public void addchild(Widget child, Object... args) {
        add(child);
        Coord c = (Coord) args[0];
        if (child instanceof GItem) {
            // convert multi-row coordinate into single row
            c.x = isz.x * c.y + c.x;
            c.y = 0;
            GItem i = (GItem) child;
            wmap.put(i, add(new WItem(i), c.mul(sqsz).add(1, 1)));
        }
    }

    @Override
    public void cdestroy(Widget w) {
        super.cdestroy(w);
        if (w instanceof GItem) {
            GItem i = (GItem) w;
            ui.destroy(wmap.remove(i));
        }
    }

    @Override
    public boolean drop(Coord cc, Coord ul) {
        Coord dc = dropul ? ul.add(sqsz.div(2)).div(sqsz) : cc.div(sqsz);
        // convert single row coordinate into multi-row
        if (dc.x >= isz.x) {
            dc.y = dc.x / isz.x;
            dc.x = dc.x % isz.x;
        }
        wdgmsg("drop", dc);
        return(true);
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul) {
        return (false);
    }

    @Override
    public void uimsg(String msg, Object... args) {
        if (msg == "sz") {
            isz = (Coord) args[0];
            resize(invsq.sz().add(new Coord(-1, -1)).mul(isz).add(new Coord(1, 1)));
        } else if(msg == "mode") {
            dropul = (((Integer)args[0]) == 0);
        } else {
            super.uimsg(msg, args);
        }
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if(!msg.endsWith("-identical"))
            super.wdgmsg(sender, msg, args);
    }

    /* Following getItem* methods do partial matching of the name *on purpose*.
       Because when localization is turned on, original English name will be in the brackets
       next to the translation
    */
    public List<WItem> getItemsPartial(String... names) {
        List<WItem> items = new ArrayList<WItem>();
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem) {
                String wdgname = ((WItem)wdg).item.getname();
                for (String name : names) {
                    if (wdgname.contains(name)) {
                        items.add((WItem) wdg);
                        break;
                    }
                }
            }
        }
        return items;
    }

    public WItem getItemPartial(String name) {
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem) {
                String wdgname = ((WItem)wdg).item.getname();
                if (wdgname.contains(name))
                    return (WItem) wdg;
            }
        }
        return null;
    }

    public boolean drink(int threshold) {
        IMeter.Meter stam = gameui().getmeter("stam", 0);
        if (stam == null || stam.a > threshold)
            return false;

        List<WItem> containers = getItemsPartial("Waterskin", "Waterflask", "Kuksa");
        for (WItem wi : containers) {
            ItemInfo.Contents cont = wi.item.getcontents();
            if (cont != null) {
                FlowerMenu.setNextSelection("Drink");
                ui.lcc = wi.rootpos();
                wi.item.wdgmsg("iact", wi.c, 0);
                return true;
            }
        }

        return false;
    }
}
