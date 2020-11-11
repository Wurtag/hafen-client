package haven;

public class WidgetVerticalAppender {
    public WidgetVerticalAppender(Widget widget) {
        this.widget = widget;
        this.verticalMargin = 0;
        this.horizontalMargin = 0;
        this.y = 0;
        this.x = 0;
    }

    public void setX(int value) {
        this.x = value;
    }

    public void setVerticalMargin(int value) {
        verticalMargin = value;
    }

    public void setHorizontalMargin(int value) {
        horizontalMargin = value;
    }

    public <T extends Widget> void add(T child) {
        widget.add(child, new Coord(x, y));
        y += child.sz.y + verticalMargin;
    }

    public void addRow(Widget ... children) {
        int x = this.x;
        int maxHeight = 0;
        for (Widget child : children) {
            widget.add(child, new Coord(x, y));
            x += child.sz.x + horizontalMargin;
            if (maxHeight < child.sz.y) {
                maxHeight = child.sz.y;
            }
        }
        y += maxHeight + verticalMargin;
    }

    public void addRow(int firstChildWidth, Widget ... children) {
        int x = this.x;
        int maxHeight = 0;

        // first widget without padding
        Widget first = children[0];

        int padding = firstChildWidth - first.sz.x;
        if (padding < 0)
            padding = 0;

        widget.add(first, new Coord(x, y));
        x += first.sz.x + horizontalMargin + padding;
        if (maxHeight < first.sz.y) {
            maxHeight = first.sz.y;
        }

        for (int i = 1; i < children.length; i++) {
            Widget child = children[i];
            widget.add(child, new Coord(x, y));
            x += child.sz.x + horizontalMargin;
            if (maxHeight < child.sz.y) {
                maxHeight = child.sz.y;
            }

        }
        y += maxHeight + verticalMargin;
    }

    private final Widget widget;
    private int verticalMargin;
    private int horizontalMargin;
    private int y;
    private int x;
}
