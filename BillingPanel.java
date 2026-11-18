package retail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Billing screen: search products, build a cart, generate the bill. */
public class BillingPanel extends JPanel {

    private final ProductDAO productDao = new ProductDAO();
    private final BillingDAO billingDao = new BillingDAO();
    private final Cart cart = new Cart();

    private final DefaultTableModel productModel = readOnlyModel(
            new Class<?>[]{Integer.class, String.class, BigDecimal.class, Integer.class},
            "ID", "Product", "Price", "In stock");
    private final DefaultTableModel cartModel = readOnlyModel(
            new Class<?>[]{Integer.class, String.class, BigDecimal.class, Integer.class, BigDecimal.class},
            "ID", "Product", "Price", "Qty", "Subtotal");

    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable    = new JTable(cartModel);
    private final TableRowSorter<DefaultTableModel> productSorter = new TableRowSorter<>(productModel);

    private final JTextField searchField = Theme.field(14);
    private final JSpinner qtySpinner    = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    private final JLabel totalLabel      = new JLabel("0.00");

    public BillingPanel() {
        setLayout(new BorderLayout(0, Theme.px(14)));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(Theme.px(16), Theme.px(20), Theme.px(16), Theme.px(20)));

        JPanel main = new JPanel(new GridLayout(1, 2, Theme.px(14), 0));
        main.setOpaque(false);
        main.add(buildProductsCard());
        main.add(buildCartCard());
        add(main, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        refresh();
    }

    // ------------------------------------------------------------ UI building

    private JPanel buildProductsCard() {
        Theme.styleTable(productTable);
        productTable.setRowSorter(productSorter);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getColumnModel().getColumn(0).setMaxWidth(Theme.px(70));
        int[] pw = {70, 300, 110, 110};   // relative column widths: ID, Product, Price, In stock
        for (int i = 0; i < pw.length; i++) productTable.getColumnModel().getColumn(i).setPreferredWidth(Theme.px(pw[i]));
        productTable.addMouseListener(new MouseAdapter() {      // double-click adds to cart
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = productTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        productTable.setRowSelectionInterval(viewRow, viewRow);
                        addToCart();
                    }
                }
            }
        });

        searchField.setToolTipText("Type to search products by name");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        JPanel top = new JPanel(new BorderLayout(Theme.px(12), 0));
        top.setOpaque(false);
        top.add(Theme.title("Products"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(Theme.muted("\u2190 search"), BorderLayout.EAST);

        Theme.styleSpinner(qtySpinner);
        JButton addBtn = Theme.button("Add to cart  \u2192", Theme.PRIMARY);
        addBtn.addActionListener(e -> addToCart());
        JLabel qtyLabel = Theme.fieldLabel("QUANTITY");
        JPanel pick = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.px(10), 0));
        pick.setOpaque(false);
        pick.add(qtyLabel);
        pick.add(qtySpinner);
        pick.add(addBtn);

        JPanel card = Theme.card(new BorderLayout(0, Theme.px(10)));
        card.add(top, BorderLayout.NORTH);
        card.add(Theme.scroll(productTable), BorderLayout.CENTER);
        card.add(pick, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildCartCard() {
        Theme.styleTable(cartTable);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(Theme.px(70));
        int[] cw = {70, 260, 110, 80, 130};   // ID, Product, Price, Qty, Subtotal
        for (int i = 0; i < cw.length; i++) cartTable.getColumnModel().getColumn(i).setPreferredWidth(Theme.px(cw[i]));

        JButton removeBtn = Theme.button("Remove line", Theme.NEUTRAL);
        JButton clearBtn  = Theme.button("Clear cart", Theme.DANGER);
        removeBtn.addActionListener(e -> removeLine());
        clearBtn.addActionListener(e -> { cart.clear(); refreshCart(); });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(removeBtn);
        buttons.add(clearBtn);

        JPanel card = Theme.card(new BorderLayout(0, Theme.px(10)));
        card.add(Theme.title("Cart"), BorderLayout.NORTH);
        card.add(Theme.scroll(cartTable), BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBottomBar() {
        JLabel caption = new JLabel("TOTAL");
        caption.setFont(Theme.font(Font.BOLD, 12));
        caption.setForeground(new Color(0x94A3B8));
        totalLabel.setFont(Theme.font(Font.BOLD, 30));
        totalLabel.setForeground(Color.WHITE);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel totalBox = new JPanel();
        totalBox.setOpaque(false);
        totalBox.setLayout(new BoxLayout(totalBox, BoxLayout.Y_AXIS));
        totalBox.add(caption);
        totalBox.add(totalLabel);

        JButton billBtn = Theme.button("Generate Bill", Theme.SUCCESS);
        billBtn.setFont(Theme.font(Font.BOLD, 18));
        billBtn.setBorder(new EmptyBorder(Theme.px(12), Theme.px(40), Theme.px(12), Theme.px(40)));
        billBtn.addActionListener(e -> generateBill());

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.HEADER);
        bar.setBorder(new EmptyBorder(Theme.px(12), Theme.px(20), Theme.px(12), Theme.px(20)));
        bar.add(totalBox, BorderLayout.WEST);
        bar.add(billBtn, BorderLayout.EAST);
        return bar;
    }

    // ------------------------------------------------------------ actions

    public void refresh() {
        productModel.setRowCount(0);
        try {
            for (Product p : productDao.getAll())
                productModel.addRow(new Object[]{p.id(), p.name(), p.price(), p.stockQty()});
        } catch (ShopException e) {
            Ui.error(this, e);
        }
    }

    private void applyFilter() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) productSorter.setRowFilter(null);
        else productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1));
    }

    private void addToCart() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) { warn("Select a product first."); return; }
        int row = productTable.convertRowIndexToModel(viewRow);

        Product p = new Product(
                (int) productModel.getValueAt(row, 0),
                (String) productModel.getValueAt(row, 1),
                (BigDecimal) productModel.getValueAt(row, 2),
                (int) productModel.getValueAt(row, 3));
        try {
            cart.add(p, (int) qtySpinner.getValue());   // Cart enforces quantity > 0 and the stock limit
            qtySpinner.setValue(1);
            refreshCart();
        } catch (ShopException e) {
            Ui.error(this, e);
        }
    }

    private void removeLine() {
        int viewRow = cartTable.getSelectedRow();
        if (viewRow < 0) { warn("Select a cart line first."); return; }
        int row = cartTable.convertRowIndexToModel(viewRow);
        cart.remove((int) cartModel.getValueAt(row, 0));
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (Cart.Line l : cart.lines())
            cartModel.addRow(new Object[]{l.productId(), l.name(), l.price(), l.qty(), l.subtotal()});
        totalLabel.setText(cart.total().toPlainString());
    }

    private void generateBill() {
        if (cart.isEmpty()) { warn("Cart is empty."); return; }
        try {
            Bill bill = billingDao.generateBill(cart);   // one transaction: sale + items + stock
            JTextArea area = new JTextArea(bill.receipt());
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Theme.px(14)));
            area.setEditable(false);
            area.setBorder(new EmptyBorder(Theme.px(8), Theme.px(8), Theme.px(8), Theme.px(8)));
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(Theme.px(500), Theme.px(360)));
            JOptionPane.showMessageDialog(this, sp, "Bill generated", JOptionPane.INFORMATION_MESSAGE);
            cart.clear();
            refreshCart();
            refresh();
        } catch (ShopException e) {
            Ui.error(this, e);
            refresh();                      // show the latest stock figures
        }
    }

    private static DefaultTableModel readOnlyModel(Class<?>[] types, String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return types[c]; }
        };
    }

    private void warn(String m)  { JOptionPane.showMessageDialog(this, m, "Notice", JOptionPane.WARNING_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error",  JOptionPane.ERROR_MESSAGE); }
}
