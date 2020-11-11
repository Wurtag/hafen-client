package haven;

import java.awt.*;
import java.awt.event.KeyEvent;

import static haven.Inventory.invsq;

public class FBelt extends Widget implements DTarget, DropTarget {
    private final int beltkeys[] = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
            KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
            KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    @SuppressWarnings("unchecked")
    private GameUI.BeltSlot[] belt = new GameUI.BeltSlot[12];
    private UI.Grab dragging;
    private Coord dc;
    private static final Coord vsz = new Coord(34, 450);
    private static final Coord hsz = new Coord(450, 34);
    private boolean vertical;
    private String chrid;
    private static final int SERVER_FSLOT_INDEX = 132;
    public static final Color keysClr = new Color(156, 180, 158, 255);

    public FBelt(String chrid, boolean vertical) {
        super(vertical ? vsz : hsz);
        this.chrid = chrid;
        this.vertical = vertical;
    }

    public void loadLocal() {
        if (chrid != "") {
            String[] resnames = Utils.getprefsa("fbelt_" + chrid, null);
            if (resnames != null) {
                for (int i = 0; i < 12; i++) {
                    String resname = resnames[i];
                    if (!resname.equals("null")) {
                        try {
                            belt[i] = ((GameUI)parent).new BeltSlot(i, Resource.local().load(resname), Message.nil);
                        } catch (Exception e) {   // possibly a resource from another client
                        }
                    }
                }
            }
        }
    }

    private void saveLocally() {
        String chrid = gameui().chrid;
        if (chrid != "") {
            String[] resnames = new String[12];
            for (int i = 0; i < 12; i++) {
                try {
                    GameUI.BeltSlot res = belt[i];
                    if (res != null && res.getres().name.startsWith("paginae/amber"))
                        resnames[i] = res.getres().name;
                } catch (Exception e) {
                }
            }
            Utils.setprefsa("fbelt_" + chrid, resnames);
        }
    }

    private Coord beltc(int i) {
        if (vertical)
            return new Coord(0, ((invsq.sz().x + 2) * i) + (10 * (i / 4)));
        return new Coord(((invsq.sz().x + 2) * i) + (10 * (i / 4)), 0);
    }

    private int beltslot(Coord c) {
        for (int i = 0; i < 12; i++) {
            if (c.isect(beltc(i), invsq.sz()))
                return i;
        }
        return -1;
    }

    @Override
    public void draw(GOut g) {
        for (int slot = 0; slot < 12; slot++) {
            Coord c = beltc(slot);
            g.image(invsq, c);
            try {
                GameUI.BeltSlot ires = belt[slot];
                if (ires != null)
                    g.image(ires.getres().layer(Resource.imgc).tex(), c.add(1, 1));
            } catch (Loading e) {
            } catch (Exception re) {
                // possibly a resource from another client
                belt[slot] = null;
            }
            g.chcolor(keysClr);
            FastText.aprint(g, new Coord(c.x + invsq.sz().x - 2, c.y + invsq.sz().y), 1, 1, "F" + (slot + 1));
            g.chcolor();
        }
    }

    @Override
    public boolean mousedown(Coord c, int button) {
        int slot = beltslot(c);
        if (slot != -1) {
            if (belt[slot] == null) {
                if (ui.modshift) {
                    if (vertical) {
                        sz = hsz;
                        vertical = false;
                    } else {
                        sz = vsz;
                        vertical = true;
                    }
                    Utils.setprefb("fbelt_vertical", vertical);
                } else {
                    dragging = ui.grabmouse(this);
                    dc = c;
                }
                return true;
            }

            if (button == 1) {
                use(slot);
            } else if (button == 3) {
                GameUI.BeltSlot res = belt[slot];
                if (res != null) {
                    try {
                        if (!res.getres().name.startsWith("paginae/amber"))
                            gameui().wdgmsg("setbelt", getServerSlot(slot), 1);
                    } catch (Exception e) {
                    }
                    belt[slot] = null;
                    saveLocally();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseup(Coord c, int button) {
        if (dragging != null) {
            dragging.remove();
            dragging = null;
            Utils.setprefc("fbelt_c", this.c);
            return true;
        }
        return super.mouseup(c, button);
    }

    @Override
    public void mousemove(Coord c) {
        if (dragging != null) {
            this.c = this.c.add(c.x, c.y).sub(dc);
            return;
        }
        super.mousemove(c);
    }

    @Override
    public boolean globtype(char key, KeyEvent ev) {
        if (key != 0)
            return false;
        for (int i = 0; i < beltkeys.length; i++) {
            if (ev.getKeyCode() == beltkeys[i]) {
                if (belt[i] != null)
                    use(i);
                return true;
            }
        }
        return false;
    }

    public boolean drop(Coord c, Coord ul) {
        int slot = beltslot(c);
        if (slot != -1) {
            GameUI gui = gameui();
            WItem item = gui.vhand;
            if (item != null && item.item != null) {
                belt[slot] = gui.new BeltSlot(slot, item.item.res, Message.nil);
                gui.wdgmsg("setbelt", getServerSlot(slot), 0);
                saveLocally();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean iteminteract(Coord c, Coord ul) {
        return false;
    }

    public boolean dropthing(Coord c, Object thing) {
        int slot = beltslot(c);
        if (slot != -1) {
            if (thing instanceof Resource) {
                Resource res = (Resource) thing;
                if (res.layer(Resource.action) != null) {
                    belt[slot] = this.gameui().new BeltSlot(slot, res.indir(), Message.nil);
                    if (res.name.startsWith("paginae/amber"))
                        saveLocally();
                    else
                        gameui().wdgmsg("setbelt", getServerSlot(slot), res.name);
                    return true;
                }
            }
        }
        return false;
    }

    private void use(int slot) {
        try {
            Resource res = belt[slot].getres();
            Resource.AButton act = res.layer(Resource.action);
            if (act == null) {
                gameui().wdgmsg("belt", getServerSlot(slot), 1, ui.modflags());
            } else {
                if (res.name.startsWith("paginae/amber"))
                    gameui().menu.use(act.ad);
                else
                    gameui().act(act.ad);
            }
        } catch (Exception e) {
        }
    }

    private int getServerSlot(int slot) {
        return SERVER_FSLOT_INDEX + slot;
    }

    public void delete(int serverSlot) {
        if (serverSlot < SERVER_FSLOT_INDEX)
            return;

        int slot = serverSlot - SERVER_FSLOT_INDEX;

        GameUI.BeltSlot ires = belt[slot];
        try {
            if (ires == null || ires.getres().name.startsWith("paginae/amber"))
                return;
        } catch (Exception e) {
        }

        belt[slot] = null;
        saveLocally();
    }

    public void add(int serverSlot, GameUI.BeltSlot res) {
        if (serverSlot < SERVER_FSLOT_INDEX)
            return;
        belt[serverSlot - SERVER_FSLOT_INDEX] = res;
    }
}