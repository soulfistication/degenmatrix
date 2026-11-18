package degenmatrix.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class UISwitch extends JToggleButton {
    private float thumbPosition = 0.0f;
    private Timer animator;
    private final int animationDuration = 150;
    private long startTime;

    // Neutral/Accent colors suited for toggling between two active formats
    private Color offBackground = new Color(180, 185, 195);
    private Color onBackground = new Color(52, 120, 246); // iOS Blue
    private Color thumbColor = Color.WHITE;
    private Color borderColor = new Color(160, 165, 175);

    public UISwitch() {
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(46, 25)); // Compact height to match JRadioButtons

        thumbPosition = isSelected() ? 1.0f : 0.0f;
        addActionListener(e -> startAnimation());
    }

    private void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.stop();
        }

        startTime = System.currentTimeMillis();
        final float startPos = thumbPosition;
        final float endPos = isSelected() ? 1.0f : 0.0f;

        animator = new Timer(15, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = (float) elapsed / animationDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                animator.stop();
            }
            thumbPosition = startPos + (endPos - startPos) * easeOut(progress);
            repaint();
        });
        animator.start();
    }

    private float easeOut(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = height;

        Color currentBg = blendColors(offBackground, onBackground, thumbPosition);
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));

        if (thumbPosition < 0.5f) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, width - 1.2f, height - 1.2f, arc - 1.2f, arc - 1.2f));
        }

        int padding = 2;
        int thumbSize = height - (padding * 2);
        float xMin = padding;
        float xMax = width - thumbSize - padding;
        float currentX = xMin + (xMax - xMin) * thumbPosition;

        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval((int) currentX, padding + 1, thumbSize, thumbSize);

        g2.setColor(thumbColor);
        g2.fillOval((int) currentX, padding, thumbSize, thumbSize);

        g2.dispose();
    }

    private Color blendColors(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }
}