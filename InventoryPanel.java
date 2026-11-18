package retail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

/** Inventory screen: editable table (name / price / stock), add and remove products. */
public class InventoryPanel extends JPanel {

    private static final int LOW_STOCK = Ui.LOW_STOCK;   // stock at or below this is shown in red

    private final ProductDAO dao = new ProductDAO();
    private boolean loading = false;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col != 0; }
        @Override public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 3 -> Integer.class;
                case 2    -> BigDecimal.class;
                default   -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField  = Theme.field(20);
    private final JTextField priceField = Theme.field(8);
    private final JTextField stockField = Theme.field(6);

    public InventoryPanel() {
        setLayout(new BorderLayout(0, Theme.px(14)));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(Theme.px(16), Theme.px(20), Theme.px(16), Theme.px(20)));

        add(buildForm(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        model.addTableModelListener(this::onCellEdited);
        refresh();
    }

    // ------------------------------------------------------------ UI building

    private JPanel buildForm() {
        JPanel form = Theme.card(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        int gap = Theme.px(12);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 4;
        g.insets = new Insets(0, 0, Theme.px(10), 0);
        form.add(Theme.title("Add a new product"), g);

        // labels row
        g.gridwidth = 1; g.gridy = 1;
        g.insets = new Insets(0, 0, Theme.px(4), gap);
        g.gridx = 0; g.weightx = 1; form.add(Theme.fieldLabel("Name"), g);
        g.gridx = 1; g.weightx = 0; form.add(Theme.fieldLabel("Price"), g);
        g.gridx = 2;                form.add(Theme.fieldLabel("Stock"), g);

        // fields row
        JButton addBtn = Theme.button("Add product", Theme.PRIMARY);
        addBtn.addActionListener(e -> addProduct());
        nameField.addActionListener(e -> addProduct());    // Enter key also adds
        priceField.addActionListener(e -> addProduct());
        stockField.addActionListener(e -> addProduct());

        g.gridy = 2; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1; form.add(nameField, g);
        g.gridx = 1; g.weightx = 0; form.add(priceField, g);
        g.gridx = 2;                form.add(stockField, g);
        g.gridx = 3; g.insets = new Insets(0, 0, 0, 0); g.fill = GridBagConstraints.VERTICAL;
        form.add(addBtn, g);
        return form;
    }

    private JPanel buildTableCard() {
        Theme.styleTable(table);
        Ui.useTextEditors(table);
        table.getColumnModel().getColumn(0).setMaxWidth(Theme.px(80));
        // low-stock numbers in bold red
        table.getColumnModel().getColumn(3).setCellRenderer(new Theme.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof Integer q && q <= LOW_STOCK) {
                    setForeground(Theme.DANGER);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return comp;
            }
        });

        JLabel title = Theme.title("Products");
        JLabel hint = Theme.muted("Double-click a Name, Price or Stock cell to edit it. Low stock (" + LOW_STOCK + " or fewer) shows in red.");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(Theme.px(2)));
        titles.add(hint);

        JButton refreshBtn = Theme.button("Refresh", Theme.NEUTRAL);
        JButton removeBtn  = Theme.button("Remove selected", Theme.DANGER);
        refreshBtn.addActionListener(e -> refresh());
        removeBtn.addActionListener(e -> removeSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(refreshBtn);
        buttons.add(removeBtn);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(titles, BorderLayout.WEST);
        head.add(buttons, BorderLayout.EAST);

        JPanel card = Theme.card(new BorderLayout(0, Theme.px(10)));
        card.add(head, BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------ actions

    public void refresh() {
        loading = true;
        try {
            model.setRowCount(0);
            for (Product p : dao.getAll())
                model.addRow(new Object[]{p.id(), p.name(), p.price(), p.stockQty()});
        } catch (ShopException e) {
            showError(e);
        } finally {
            loading = false;
        }
    }

    private void addProduct() {
        try {
            dao.add(nameField.getText(), priceField.getText(), stockField.getText());  // DAO validates
            nameField.setText(""); priceField.setText(""); stockField.setText("");
            nameField.requestFocusInWindow();
            refresh();
        } catch (ShopException e) {
            showError(e);
        }
    }

    private void removeSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { info("Select a product first."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        String name = String.valueOf(model.getValueAt(row, 1));
        if (JOptionPane.showConfirmDialog(this, "Remove '" + name + "' from inventory?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.remove(id);
            refresh();
        } catch (ShopException e) {
            showError(e);
        }
    }

    /** Fires when the user edits a cell: validate, save to DB, then reload to normalise formatting. */
    private void onCellEdited(TableModelEvent e) {
        if (loading || e.getType() != TableModelEvent.UPDATE || e.getColumn() < 1) return;
        int row = e.getFirstRow(), col = e.getColumn();
        int id = (int) model.getValueAt(row, 0);
        String raw = String.valueOf(model.getValueAt(row, col));
        try {
            switch (col) {
                case 1 -> dao.rename(id, raw);
                case 2 -> dao.changePrice(id, raw);
                case 3 -> dao.changeStock(id, raw);
            }
        } catch (ShopException ex) {
            showError(ex);
        }
        SwingUtilities.invokeLater(this::refresh); // reload either way (reverts bad edits)
    }

    private void showError(Exception e) {
        Ui.error(this, e);
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m);
    }
}
