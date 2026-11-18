package retail;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();

            // fail early with a friendly message if MySQL isn't reachable
            try {
                DBConnection.test();
            } catch (ShopException e) {
                Ui.error(null, e);
                return;
            }

            InventoryPanel inventory = new InventoryPanel();
            BillingPanel billing = new BillingPanel();

            // two "cards" (screens); the nav buttons decide which one is visible
            CardLayout cards = new CardLayout();
            JPanel content = new JPanel(cards);
            content.setBackground(Theme.BG);
            content.add(inventory, "inventory");
            content.add(billing, "billing");

            JToggleButton invBtn  = Theme.navButton("Inventory");
            JToggleButton billBtn = Theme.navButton("Billing");
            ButtonGroup group = new ButtonGroup();
            group.add(invBtn);
            group.add(billBtn);
            invBtn.setSelected(true);
            // always show fresh data when switching screens
            invBtn.addActionListener(e -> { inventory.refresh(); cards.show(content, "inventory"); });
            billBtn.addActionListener(e -> { billing.refresh();  cards.show(content, "billing"); });

            // top bar
            JLabel title = new JLabel("Retail Inventory & Billing");
            title.setFont(Theme.font(Font.BOLD, 20));
            title.setForeground(Color.WHITE);

            JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.px(8), 0));
            nav.setOpaque(false);
            nav.add(invBtn);
            nav.add(billBtn);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Theme.HEADER);
            header.setBorder(new EmptyBorder(Theme.px(12), Theme.px(20), Theme.px(12), Theme.px(20)));
            header.add(title, BorderLayout.WEST);
            header.add(nav, BorderLayout.EAST);

            JPanel root = new JPanel(new BorderLayout());
            root.add(header, BorderLayout.NORTH);
            root.add(content, BorderLayout.CENTER);

            JFrame f = new JFrame("Retail Inventory & Billing");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(root);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            f.setSize(Math.min(Theme.px(1000), screen.width - 40),
                      Math.min(Theme.px(620),  screen.height - 80));
            f.setLocationRelativeTo(null);
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setVisible(true);
        });
    }
}
