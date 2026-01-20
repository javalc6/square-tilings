import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/*
License Information, 2025 Livio (javalc6)

Feel free to modify, re-use this software, please give appropriate
credit by referencing this Github repository.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

IMPORTANT NOTICE
Note that this software is freeware and it is not designed, licensed or
intended for use in mission critical, life support and military purposes.
The use of this software is at the risk of the user. 

DO NOT USE THIS SOFTWARE IF YOU DON'T AGREE WITH STATED CONDITIONS.
*/
import java.io.File;
import java.io.IOException;

/**
 * Swing application to visualize square tilings.
 * UI to select the tile, the size of the tile, colors and to save the image on file.
 *
 * v1.0, 23-01-2026: Square Tiling first release
 *
 *
 * TODO: add Custom tiles
 */

public class SquareTiling extends JFrame {

    enum TileType { GREEK, IPATTERN1, IPATTERN2, IPATTERN3, CROSSED, INTERLACED, OCTAGRAM1, OCTAGRAM2, OCTAGON, SQUARES, CHECKERED, TARTAN, }
    private TileType currentType = TileType.values()[0];
    private final static String[] tileTitles = {
        "Greek Pattern", "Layered Islamic Star", "Quartered Islamic Star", "Eightfold Islamic Star", 
        "Crossed Square", "Interlaced Circles", "Large Octagram", "Narrow Octagram", "Octagon & Rectangles", 
			"Squares and rhombus", "Checkered", "Tartan",
    };

    private int tileSize = 150;

    private Color colorA = new Color(255, 180, 0);  // Gold
    private Color colorB = new Color(0, 20, 60); // Deep Blue
    private Color colorC = Color.WHITE;
    private Color colorD = new Color(0, 200, 210); // Cyan

    private TilingPanel tilingPanel;

    public SquareTiling() {
        setTitle("Geometric Square Tiling Generator");
        setSize(1000, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tilingPanel = new TilingPanel();

        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JComboBox<TileType> typeCombo = new JComboBox<>(TileType.values());
        typeCombo.setSelectedItem(currentType);
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TileType) setText(tileTitles[((TileType) value).ordinal()]);
                return this;
            }
        });
        typeCombo.addActionListener(e -> {
            currentType = (TileType) typeCombo.getSelectedItem();
            tilingPanel.clearCache();
            repaint();
        });

        JButton showTileBtn = new JButton("Show Tile");
        showTileBtn.addActionListener(e -> showSingleTile());

        JButton saveBtn = new JButton("Save Image");
        saveBtn.addActionListener(e -> saveImage());

        addControl(topPanel, "Tile Pattern:", typeCombo);
        topPanel.add(showTileBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(saveBtn);

        JPanel sidebar = new JPanel();
        sidebar.setBackground(Color.DARK_GRAY);
        sidebar.setPreferredSize(new Dimension(80, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 10, 10, 10));

        JLabel sizeLabel = new JLabel("Tile Size");
        sizeLabel.setForeground(Color.WHITE);
        sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSlider sizeSlider = new JSlider(JSlider.VERTICAL, 40, 400, 150);
        sizeSlider.setBackground(Color.DARK_GRAY);
        sizeSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        sizeSlider.addChangeListener(e -> {
            tileSize = sizeSlider.getValue();
            tilingPanel.clearCache();
            repaint();
        });

        JLabel colorLabel = new JLabel("Colors");
        colorLabel.setForeground(Color.WHITE);
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorLabel.setBorder(new EmptyBorder(20, 0, 10, 0));

        JButton[] colorButtons = {
            createColorButton(colorA, c -> colorA = c),
            createColorButton(colorB, c -> colorB = c),
            createColorButton(colorC, c -> colorC = c),
            createColorButton(colorD, c -> colorD = c)
        };

        sidebar.add(sizeLabel);
        sidebar.add(sizeSlider);
        sidebar.add(colorLabel);
        for (JButton btn : colorButtons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing between buttons
        }

        add(topPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(tilingPanel, BorderLayout.CENTER);
    }

    class TilingPanel extends JPanel {
        private BufferedImage cachedTile;

        public void clearCache() { cachedTile = null; }

        private void updateCache() {
            cachedTile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = cachedTile.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            drawTile(g2, 0, 0, tileSize);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            if (cachedTile == null) updateCache();

            for (int y = 0; y < getHeight(); y += tileSize) {
                for (int x = 0; x < getWidth(); x += tileSize) {
                    g2d.drawImage(cachedTile, x, y, null);
                }
            }
        }
    }

    private void drawTile(Graphics2D g2d, int x, int y, int size) {
        switch (currentType) {
            case GREEK: drawGreekTile(g2d, x, y, size); break;
            case IPATTERN1: drawIslamicStarTile1(g2d, x, y, size); break;
            case IPATTERN2: drawIslamicStarTile2(g2d, x, y, size); break;
            case IPATTERN3: drawIslamicStarTile3(g2d, x, y, size); break;
            case CROSSED: drawCrossedTile(g2d, x, y, size); break;
            case INTERLACED: drawInterlacedTile(g2d, x, y, size); break;
            case OCTAGRAM1: drawOctagramTile(g2d, x, y, 0.384, size); break;
            case OCTAGRAM2: drawOctagramTile(g2d, x, y, 0.27, size); break;
            case OCTAGON: drawOctagonTile(g2d, x, y, size); break;
            case CHECKERED: drawCheckeredTile(g2d, x, y, size); break;
            case TARTAN: drawTartanTile(g2d, x, y, size); break;
            case SQUARES: drawSquaresTile(g2d, x, y, size); break;
            default: break;
        }
    }

    private void drawGreekTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorC);
        g2d.fillRect(x, y, x + size, y + size);

        double step = size / 8.0;
        double[][] path = {{0,1}, {1,1}, {1,7}, {6,7}, {6,4}, {5,4}, {5,6}, {2,6}, {2,1}, {8,1}, {8,2}, {3,2}, {3,5}, {4,5}, {4,3}, {7,3}, {7,8}, {0,8}};
        Path2D p = new Path2D.Double();
        p.moveTo(step * path[0][0], step * path[0][1]);
        for (int i = 1; i < path.length; i++)
            p.lineTo(step * path[i][0], step * path[i][1]);
        p.closePath();

        g2d.setColor(colorB); 
        g2d.fill(p);
        g2d.draw(p);
    }

    private void drawIslamicStarTile1(Graphics2D g2d, int x, int y, int size) {
        drawOctagramTile(g2d, x, y, 0.384, size);

        double cx = x + size / 2.0; double cy = y + size / 2.0;
        Path2D star = createStar(cx, cy, size * 0.287, size * 0.375, 8, Math.PI / 8);
        g2d.setColor(colorD); g2d.fill(star);

        star = createStar(cx, cy, size * 0.155, size * 0.287, 8, 0);
        g2d.setColor(colorB); g2d.fill(star);
    }

    private void drawIslamicStarTile2(Graphics2D g2d, int x, int y, int size) {
        double cx = x + size / 2.0; double cy = y + size / 2.0;
        g2d.setColor(colorD);
        g2d.fillRect(x, y, x + size, y + size);

        Path2D star = createStar(cx, cy, size * 0.27, size * 0.5, 8, 0);
        g2d.setColor(colorB); g2d.fill(star);

        double r = size * 0.21;
        Path2D[] stars = {
            createStar(x, y, r * 0.5, r, 4, Math.PI/4), createStar(x + size, y, r * 0.5, r, 4, Math.PI/4),
            createStar(x, y + size, r * 0.5, r, 4, Math.PI/4), createStar(x + size, y + size, r * 0.5, r, 4, Math.PI/4),
        };
        for (Path2D s : stars) { g2d.fill(s); g2d.draw(s); }

        Path2D star2 = createStar(cx, cy, size * 0.207, size * 0.27, 8, Math.PI/8);
        g2d.setColor(colorA); g2d.fill(star2);

    }

    private void drawIslamicStarTile3(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        double cx = x + size / 2.0; double cy = y + size / 2.0;
        float strokeSize = Math.max(1.5f, size / 80f); g2d.setStroke(new BasicStroke(strokeSize));
        g2d.setColor(colorC);
        double r = size * 0.15;
        Path2D[] stars = {
            createStar(x, y, r * 0.5, r, 4, Math.PI/4), createStar(x + size, y, r * 0.5, r, 4, Math.PI/4),
            createStar(x, y + size, r * 0.5, r, 4, Math.PI/4), createStar(x + size, y + size, r * 0.5, r, 4, Math.PI/4),
            createStar(cx, y, r * 0.5, r, 4, 0), createStar(cx, y + size, r * 0.5, r, 4, 0),
            createStar(x, cy, r * 0.5, r, 4, 0), createStar(x + size, cy, r * 0.5, r, 4, 0)
        };
        for (Path2D s : stars) { g2d.fill(s); g2d.draw(s); }
        Path2D mainStar = createStar(cx, cy, size * 0.19, size * 0.35, 8, 0);
        g2d.setColor(colorA); g2d.fill(mainStar);
        g2d.setColor(colorC); g2d.draw(mainStar);
    }

    private void drawCrossedTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        g2d.setStroke(new BasicStroke(Math.max(1, size / 30f)));
        g2d.setColor(colorA); g2d.drawLine(x, y, x + size, y + size); g2d.drawLine(x + size, y, x, y + size);
        g2d.setColor(colorC); g2d.drawRect(x + size/4, y + size/4, size/2, size/2);
    }

    private void drawInterlacedTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        g2d.setStroke(new BasicStroke(Math.max(1, size / 40f)));
        g2d.setColor(colorA); int r = size / 2;
        g2d.drawArc(x - r, y - r, size, size, 0, -90); g2d.drawArc(x + r, y - r, size, size, 180, 90);
        g2d.drawArc(x - r, y + r, size, size, 0, 90); g2d.drawArc(x + r, y + r, size, size, 180, -90);
        g2d.setColor(colorC); g2d.drawOval(x, y, size, size);
    }

    private void drawOctagramTile(Graphics2D g2d, int x, int y, double factor, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        double cx = x + size / 2.0; double cy = y + size / 2.0;
        Path2D star = createStar(cx, cy, size * factor, size * 0.5, 8, 0);
        g2d.setColor(colorA); g2d.fill(star);
        g2d.setColor(colorC); g2d.setStroke(new BasicStroke(Math.max(1, size / 60f)));
        g2d.draw(star);
    }

    private void drawOctagonTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        double cx = x + size / 2.0; double cy = y + size / 2.0;
        double d = 1.0 / (3.0 + Math.sqrt(2));
        Path2D octagon = createPolygon(cx, cy, size * d, 8, 0);
        g2d.setColor(colorA); g2d.fill(octagon);
        g2d.draw(octagon);

        double offset = d * (1.0 + 1.0 / Math.sqrt(2)) * size;
        double[][] pos = {{0, offset}, {0, -offset}, {-offset, 0}, {offset, 0}};
        for (int k = 0; k < 4; k++) {
            Path2D square = createPolygon(cx + pos[k][0], cy + pos[k][1], size * d, 4, 0);
            g2d.setColor(colorD); g2d.fill(square);
            g2d.draw(square);
        }
    }

    private void drawCheckeredTile(Graphics2D g2d, int x, int y, int size) {
        int half = size / 2;
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                g2d.setColor(i == j ? colorB : colorC);
                g2d.fillRect(x + half * i, y + half * j, x + half + half * i, y + half + half * j);
            }
    }

    private void drawTartanTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorD);
        g2d.fillRect(x, y, x + size, y + size);

        Color semiTransparentColorB = new Color(colorB.getRed(), colorB.getGreen(), colorB.getBlue(), 128);

        int half = size / 2;
        g2d.setColor(semiTransparentColorB);
        g2d.fillRect(0, 0, half, size);
        g2d.fillRect(0, 0, size, half);
        
        int quarter = half / 2;
        g2d.setColor(colorA);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(quarter, 0, quarter, size);
        g2d.drawLine(0, quarter, size, quarter);
    }

    private void drawSquaresTile(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(colorB);
        g2d.fillRect(x, y, x + size, y + size);

        double cx = x + size / 2.0; double cy = y + size / 2.0;

        double a = Math.PI / 6;
        double cosT = Math.cos(a);
        double sinT = Math.sin(a);

		double side = size / (2 * (cosT + sinT));
        double offset = (side / 2.0) * (cosT + sinT);

        g2d.setColor(colorD);
        Path2D path = createSquare(cx - offset, cy - offset, side, a);  // Top Left
        g2d.fill(path);
        path = createSquare(cx + offset, cy - offset, side, -a); // Top Right
        g2d.fill(path);
        path = createSquare(cx - offset, cy + offset, side, -a); // Bottom Left
        g2d.fill(path);
        path = createSquare(cx + offset, cy + offset, side, a);  // Bottom Right
        g2d.fill(path);
    }

    private Path2D createStar(double cx, double cy, double in, double out, int pts, double rotation) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 2 * pts; i++) {
            double r = (i % 2 == 0) ? out : in;
            double a = i * Math.PI / pts + rotation;
            double px = cx + Math.cos(a) * r, py = cy + Math.sin(a) * r;
            if (i == 0) 
                p.moveTo(px, py); 
            else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    private Path2D createPolygon(double cx, double cy, double side_length, int n_sides, double rotation) {
        Path2D p = new Path2D.Double();
        double r = side_length * 0.5 / Math.sin(Math.PI / n_sides);
        rotation -= Math.PI / n_sides;
        for (int i = 0; i < n_sides; i++) {
            double a = 2.0 * i * Math.PI / n_sides + rotation;
            double px = cx + Math.cos(a) * r, py = cy + Math.sin(a) * r;
            if (i == 0) 
                p.moveTo(px, py); 
            else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    private Path2D createSquare(double cx, double cy, double s, double angle) {
		double half = s / 2.0;
        double[][] reference_corners = {{-half, -half}, { half, -half}, { half,  half}, {-half,  half}};

        Path2D path = new Path2D.Double();

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        for (int i = 0; i < 4; i++) {
            double relX = reference_corners[i][0];
            double relY = reference_corners[i][1];

            double x = cx + relX * cosA - relY * sinA;
            double y = cy + relX * sinA + relY * cosA;

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();

		return path;
    }

    private void showSingleTile() {
        String title = tileTitles[currentType.ordinal()];
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2d.setColor(colorB);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                drawTile(g2d, x, y, size);
            }
        };

        dialog.add(previewPanel);
        dialog.setVisible(true);
    }

    private JButton createColorButton(Color initial, java.util.function.Consumer<Color> setter) {
        JButton colorBtn = new JButton();
        colorBtn.setPreferredSize(new Dimension(30, 30));
        colorBtn.setBackground(initial);
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Select Color", initial);
            if (newColor != null) {
                setter.accept(newColor); colorBtn.setBackground(newColor);
                tilingPanel.clearCache(); repaint();
            }
        });
        return colorBtn;
    }

    private void addControl(JPanel panel, String labelText, JComponent comp) {
        JLabel label = new JLabel(labelText); label.setForeground(Color.WHITE);
        panel.add(label); panel.add(comp);
    }

    private void saveImage() {
        BufferedImage image = new BufferedImage(tilingPanel.getWidth(), tilingPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics(); tilingPanel.paintAll(g2); g2.dispose();
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) 
                f = new File(f.getAbsolutePath() + ".png");
            try { 
                ImageIO.write(image, "png", f); 
            } catch (IOException ex) { 
                ex.printStackTrace(); 
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SquareTiling().setVisible(true));
    }
}