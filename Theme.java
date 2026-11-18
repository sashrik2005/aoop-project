package retail;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * One place for all colours, text sizes and styled-component helpers.
 *
 * TEXT SIZE: everything is multiplied by SCALE.
 *   default is 1.3.  Make it bigger with:  java -Dui.scale=1.6 ...   (smaller: -Dui.scale=1.0)
 */
public final class Theme {
    private Theme() {}

    public static final float SCALE = parseScale();

    private static float parseScale() {
        try { return Float.parseFloat(System.getProperty("ui.scale", "1.3")); }
        catch (NumberFormatException e) { return 1.3f; }
    }

    /** scale a pixel size */
    public static int px(int v) { return Math.round(v * SCALE); }

    /** scaled font */
    public static Font font(int style, float size) {
        return new Font(Font.SANS_SERIF, style, Math.round(size * SCALE));
    }

    // ---- colours ----
    public static final Color BG         = new Color(0xF1F5F9);
    public static final Color CARD       = Color.WHITE;
    public static final Color HEADER     = new Color(0x0F172A);
    public static final Color TABLE_HEAD = new Color(0x1E293B);
    public static final Color PRIMARY    = new Color(0x2563EB);
    public static final Color SUCCESS    = new Color(0x16A34A);
    public static final Color DANGER     = new Color(0xDC2626);
    public static final Color NEUTRAL    = new Color(0x64748B);
    public static final Color TEXT       = new Color(0x0F172A);
    public static final Color MUTED      = new Color(0x64748B);
    public static final Color BORDER     = new Color(0xCBD5E1);
    public static final Color ZEBRA      = new Color(0xF8FAFC);
    public static final Color SELECTION  = new Color(0xDBEAFE);

    /** Call once, before any component is created. */
    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }

        FontUIResource body = new FontUIResource(font(Font.PLAIN, 14));
        FontUIResource bold = new FontUIResource(font(Font.BOLD, 13));
        UIManager.put("defaultFont", body);   // Nimbus uses this one
        for (String key : new String[]{
                "Label.font", "Button.font", "TextField.font", "TextArea.font", "Spinner.font",
                "FormattedTextField.font", "Table.font", "OptionPane.messageFont",
                "OptionPane.buttonFont", "ToolTip.font"}) {
            UIManager.put(key, body);
        }
        UIManager.put("TableHeader.font", bold);
    }

    // ---------------------------------------------------------------- buttons

    /** Rounded, coloured button with hover and pressed effects. */
    public static JButton button(String text, Color base) {
        JButton b = new JButton(text) {
            private boolean hover;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = base;
                if (!isEnabled())                  c = BORDER;
                else if (getModel().isPressed())   c = base.darker().darker();
                else if (hover)                    c = base.darker();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), px(12), px(12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(font(Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(px(9), px(18), px(9), px(18)));
        return b;
    }

    /** Tab-like button for the top navigation bar. */
    public static JToggleButton navButton(String text) {
        JToggleButton b = new JToggleButton(text) {
            private boolean hover;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected())  g2.setColor(PRIMARY);
                else if (hover)    g2.setColor(new Color(0x334155));
                else               g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), px(12), px(12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(font(Font.BOLD, 15));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(px(9), px(22), px(9), px(22)));
        return b;
    }

    // ---------------------------------------------------------------- panels, labels, fields

    /** White rounded "card" panel with padding. */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(px(14), px(16), px(14), px(16))));
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 18));
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(MUTED);
        return l;
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(font(Font.PLAIN, 14));
        f.setBackground(Color.WHITE);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(px(7), px(10), px(7), px(10))));
        return f;
    }

    public static void styleSpinner(JSpinner s) {
        JTextField tf = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
        tf.setFont(font(Font.BOLD, 15));
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        s.setPreferredSize(new Dimension(px(100), px(38)));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(px(16));
        JPanel corner = new JPanel();
        corner.setBackground(TABLE_HEAD);
        sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
        return sp;
    }

    // ---------------------------------------------------------------- tables

    public static void styleTable(JTable t) {
        t.setFont(font(Font.PLAIN, 14));
        t.setRowHeight(px(34));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(SELECTION);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setDefaultRenderer(Object.class, new CellRenderer());
        t.setDefaultRenderer(Number.class, new CellRenderer());

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new HeaderRenderer());
        h.setPreferredSize(new Dimension(0, px(38)));
    }

    /** Zebra rows, padding, numbers right-aligned. */
    public static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setFont(t.getFont());
            setForeground(TEXT);
            setBackground(sel ? SELECTION : (r % 2 == 0 ? CARD : ZEBRA));
            setHorizontalAlignment(v instanceof Number ? RIGHT : LEFT);
            setBorder(new EmptyBorder(0, px(10), 0, px(10)));
            return this;
        }
    }

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setBackground(TABLE_HEAD);
            setForeground(Color.WHITE);
            setFont(font(Font.BOLD, 13));
            setBorder(new EmptyBorder(0, px(10), 0, px(10)));
            setHorizontalAlignment(Number.class.isAssignableFrom(t.getColumnClass(c)) ? RIGHT : LEFT);
            return this;
        }
    }
}
