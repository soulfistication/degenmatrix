package degenmatrix.ui;

import degenmatrix.DegenMatrixProcessor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Top-down 10–20 style cap map with one file selector per electrode.
 */
public class HeadMapPanel extends JPanel {

    private static final class ElectrodePosition {
        final int index;
        final double nx;
        final double ny;

        ElectrodePosition(int index, double nx, double ny) {
            this.index = index;
            this.nx = nx;
            this.ny = ny;
        }
    }

    private static final ElectrodePosition[] POSITIONS = {
        new ElectrodePosition(0, 0.36, 0.10),
        new ElectrodePosition(1, 0.26, 0.22),
        new ElectrodePosition(2, 0.30, 0.40),
        new ElectrodePosition(3, 0.32, 0.58),
        new ElectrodePosition(4, 0.38, 0.76),
        new ElectrodePosition(5, 0.10, 0.26),
        new ElectrodePosition(6, 0.08, 0.44),
        new ElectrodePosition(7, 0.14, 0.60),
        new ElectrodePosition(8, 0.64, 0.10),
        new ElectrodePosition(9, 0.74, 0.22),
        new ElectrodePosition(10, 0.70, 0.40),
        new ElectrodePosition(11, 0.68, 0.58),
        new ElectrodePosition(12, 0.62, 0.76),
        new ElectrodePosition(13, 0.90, 0.26),
        new ElectrodePosition(14, 0.92, 0.44),
        new ElectrodePosition(15, 0.86, 0.60),
    };

    private final JButton[] electrodeButtons = new JButton[DegenMatrixProcessor.NUMBER_OF_COLUMNS];
    private final JLabel[] pathLabels = new JLabel[DegenMatrixProcessor.NUMBER_OF_COLUMNS];
    private final File[] selectedFiles = new File[DegenMatrixProcessor.NUMBER_OF_COLUMNS];

    public HeadMapPanel(ActionListener browseListener) {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(520, 480));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        for (ElectrodePosition pos : POSITIONS) {
            int i = pos.index;
            String label = DegenMatrixProcessor.ELECTRODE_LABELS[i];

            JPanel slot = new JPanel(new GridBagLayout());
            slot.setOpaque(true);
            slot.setBackground(new Color(255, 255, 255, 230));

            JButton button = new JButton(label);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 11f));
            button.setMargin(new Insets(2, 6, 2, 6));
            button.setToolTipText("Select input .DAT for " + label);
            button.addActionListener(browseListener);
            button.putClientProperty("electrodeIndex", i);
            electrodeButtons[i] = button;

            JLabel pathLabel = new JLabel("—");
            pathLabel.setFont(pathLabel.getFont().deriveFont(9f));
            pathLabel.setForeground(new Color(80, 80, 80));
            pathLabels[i] = pathLabel;

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            slot.add(button, gbc);
            gbc.gridy = 1;
            gbc.insets = new Insets(2, 0, 0, 0);
            slot.add(pathLabel, gbc);

            add(slot);
            putClientProperty("slot_" + i, slot);
        }

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutElectrodes();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        layoutElectrodes();
    }

    private void layoutElectrodes() {
        int w = getWidth();
        int h = getHeight();
        int slotW = 88;
        int slotH = 52;
        for (ElectrodePosition pos : POSITIONS) {
            JPanel slot = (JPanel) getClientProperty("slot_" + pos.index);
            int x = (int) (pos.nx * w) - slotW / 2;
            int y = (int) (pos.ny * h) - slotH / 2;
            slot.setBounds(Math.max(4, x), Math.max(28, y), slotW, slotH);
        }
    }

    public void setElectrodeFile(int index, File file) {
        selectedFiles[index] = file;
        String name = file == null ? "—" : truncate(file.getName(), 10);
        pathLabels[index].setText(name);
        pathLabels[index].setToolTipText(file == null ? null : file.getAbsolutePath());
        electrodeButtons[index].setBackground(
                file == null ? null : new Color(198, 226, 255));
    }

    public File getElectrodeFile(int index) {
        return selectedFiles[index];
    }

    public File[] getAllElectrodeFiles() {
        return selectedFiles.clone();
    }

    public void clearAllFiles() {
        for (int i = 0; i < selectedFiles.length; i++) {
            setElectrodeFile(i, null);
        }
    }

    public int electrodeIndexFromSource(Object source) {
        if (!(source instanceof JButton)) {
            return -1;
        }
        Object idx = ((JButton) source).getClientProperty("electrodeIndex");
        return idx instanceof Integer ? (Integer) idx : -1;
    }

    public void setElectrodesEnabled(boolean enabled) {
        for (int i = 0; i < DegenMatrixProcessor.NUMBER_OF_COLUMNS; i++) {
            electrodeButtons[i].setEnabled(enabled);
        }
    }

    private static String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 1) + "…";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2 + 10;
        int rx = Math.min(getWidth(), getHeight()) / 2 - 24;
        int ry = (int) (rx * 1.15);

        g2.setColor(new Color(245, 248, 252));
        g2.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
        g2.setColor(new Color(120, 140, 170));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - rx, cy - ry, rx * 2, ry * 2);

        g2.setColor(new Color(100, 120, 150));
        int[] noseX = {cx, cx - 12, cx + 12};
        int[] noseY = {cy - ry - 4, cy - ry + 18, cy - ry + 18};
        g2.fillPolygon(noseX, noseY, 3);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.setColor(new Color(90, 90, 90));
        g2.drawString("Front", cx - 16, cy - ry - 22);
        g2.drawString("Left", 8, cy);
        g2.drawString("Right", getWidth() - 36, cy);

        g2.dispose();
    }
}
