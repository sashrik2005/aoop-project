package retail;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Small shared GUI helpers: colours, dialogs, styled components, table utilities. */
final class Ui {
    static final Color BG      = new Color(0xF3F4F6);
    static final Color ACCENT  = new Color(0x2563EB);
    static final Color DANGER  = new Color(0xDC2626);
    static final Color BORDER  = new Color(0xD1D5DB);
    static final int   LOW_STOCK = 5;

    private Ui() {}

    // ---------- dialogs ----------
    /** The single place where errors become dialogs. The GUI never builds error text itself. */
    static void error(Component parent, Throwable t) {
        boolean known = t instanceof ShopException;
        if (!known) t.printStackTrace();
        String msg = known ? t.getMessage() : "Unexpected problem: " + t;
        boolean warning = t instanceof ValidationException || t instanceof InsufficientStockException;
        JOptionPane.showMessageDialog(parent, msg, warning ? "Please check" : "Error",
                warning ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    static void notice(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // ---------- components ----------
    static JPanel transparent(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    static JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return p;
    }

    static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        return l;
    }

    static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    static JButton danger(String text) {
        JButton b = new JButton(text);
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    /** Text box that filters the given table (case-insensitive "contains") on one column. */
    static <M extends TableModel> JTextField searchField(TableRowSorter<M> sorter, int column) {
        JTextField f = new JTextField(16);
        f.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String t = f.getText().trim();
                if (t.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t), column));
            }
            @Override public void insertUpdate(DocumentEvent e)  { update(); }
            @Override public void removeUpdate(DocumentEvent e)  { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
        return f;
    }

    // ---------- tables ----------
    /** Table model with real column types (so numbers sort numerically) and chosen editable columns. */
    static DefaultTableModel model(String[] cols, Class<?>[] types, int... editableCols) {
        boolean[] editable = new boolean[cols.length];
        for (int c : editableCols) editable[c] = true;
        return new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int c)         { return types[c]; }
            @Override public boolean isCellEditable(int r, int c)   { return editable[c]; }
        };
    }

    static void styleTable(JTable t, int... preferredWidths) {
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);   // columns stretch with the window
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionBackground(new Color(0xDBEAFE));
        t.setSelectionForeground(Color.BLACK);
        for (int i = 0; i < preferredWidths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(preferredWidths[i]);
    }

    /** Plain text editors, so ALL validation happens in the backend (editors never reject input). */
    static void useTextEditors(JTable t) {
        t.setDefaultEditor(Object.class,     textEditor(JTextField.LEFT));
        t.setDefaultEditor(String.class,     textEditor(JTextField.LEFT));
        t.setDefaultEditor(Integer.class,    textEditor(JTextField.RIGHT));
        t.setDefaultEditor(BigDecimal.class, textEditor(JTextField.RIGHT));
    }

    private static DefaultCellEditor textEditor(int alignment) {
        JTextField tf = new JTextField();
        tf.setHorizontalAlignment(alignment);
        return new DefaultCellEditor(tf) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean sel, int row, int col) {
                Component c = super.getTableCellEditorComponent(table, value, sel, row, col);
                c.setFont(table.getFont());   // keeps the editor the same size as the (scaled) table
                return c;
            }
        };
    }

    /** Right-aligned stock number; low stock is shown in bold red. */
    static TableCellRenderer stockRenderer() {
        return new DefaultTableCellRenderer() {
            { setHorizontalAlignment(RIGHT); }
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                boolean low = v instanceof Integer q && q <= LOW_STOCK;
                comp.setForeground(sel ? t.getSelectionForeground() : (low ? DANGER : t.getForeground()));
                comp.setFont(t.getFont().deriveFont(low ? Font.BOLD : Font.PLAIN));
                return comp;
            }
        };
    }
}
