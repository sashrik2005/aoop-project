package retail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Makes the whole UI scale with the window: layouts stretch by themselves (BorderLayout /
 * GridBagLayout / split pane), and this class additionally scales every font and table row
 * height in proportion to the window size.
 */
final class UiScale {
    private static final double BASE_W = 1000, BASE_H = 650;   // size at which the scale is 1.0
    private static final double MIN = 0.85, MAX = 2.0;
    private static final String KEY = "retail.baseFont";

    private static double factor = 1.0;
    private static boolean applied = false;

    private UiScale() {}

    static double factor() { return factor; }

    static void install(JFrame frame) {
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { apply(frame); }
        });
        apply(frame);
    }

    private static void apply(JFrame frame) {
        Container root = frame.getContentPane();
        double s = Math.min(root.getWidth() / BASE_W, root.getHeight() / BASE_H);
        s = Math.max(MIN, Math.min(MAX, s));
        if (applied && Math.abs(s - factor) < 0.01) return;   // no visible change, skip the work
        factor = s;
        applied = true;
        scale(root, s);
        root.revalidate();
        root.repaint();
    }

    private static void scale(Component c, double s) {
        if (c instanceof JComponent jc) {
            Font base = (Font) jc.getClientProperty(KEY);       // remember the original font
            if (base == null) {
                base = jc.getFont();
                jc.putClientProperty(KEY, base);
            }
            jc.setFont(base.deriveFont((float) (base.getSize2D() * s)));
            if (jc instanceof JTable t)
                t.setRowHeight(t.getFontMetrics(t.getFont()).getHeight() + (int) Math.round(10 * s));
        }
        if (c instanceof Container ct)
            for (Component child : ct.getComponents()) scale(child, s);
    }
}
