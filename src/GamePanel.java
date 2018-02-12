import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener, MouseListener {

    Timer timer = new Timer(10, this);
    private static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static int height = Toolkit.getDefaultToolkit().getScreenSize().height - 100;
    private ArrayList<Vehicle> population;
    private ArrayList<PVector> food;
    private ArrayList<PVector> poison;
    private double[] nutrition = {0.1, -1};

    private double foodRate = 0.1;
    private double poisonRate = 0.01;
    private double factor = 1.001;
    private int maxPopulation = 20;

    public GamePanel() {
        setOpaque(true);
        setBackground(Color.DARK_GRAY);
        timer.start();
        setup();
        addMouseListener(this);
    }

    private void setup() {

        population = new ArrayList<>();
        food = new ArrayList<>();
        poison = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            population.add(new Vehicle(width / 2, height / 2));
        }

        for (int i = 0; i < 20; i++) {
            food.add(new PVector((float) Math.random() * width, (float) Math.random() * height));
        }

        for (int i = 0; i < 15; i++) {
            poison.add(new PVector((float) Math.random() * width, (float) Math.random() * height));
        }

    }

    private void draw(Graphics2D g) {
        if (Math.random() < foodRate) {
            food.add(new PVector((float) Math.random() * width, (float) Math.random() * height));
        }

        if (Math.random() < poisonRate) {
            poison.add(new PVector((float) Math.random() * width, (float) Math.random() * height));
        }

        //BIRTH CONTROL MADE EASY! (well, sort of)
        if (population.size() > maxPopulation) {
            foodRate = foodRate / factor;
            poisonRate = poisonRate * factor;
        } else if (population.size() > 0 && population.size() < maxPopulation) {
            foodRate = foodRate * factor;
            poisonRate = poisonRate / factor;
        } else {
            foodRate = 0.1;
            poisonRate = 0.01;
        }

        for (int i = 0; i < population.size(); i++) {
            Vehicle v = population.get(i);
            v.eat(food, 0, nutrition);
            v.eat(poison, 1, nutrition);
            v.boundaries(width, height);
            v.update();
            v.display(population, i, g);

            if (v.dead()) {
                population.remove(i);
            } else {
                Vehicle child = v.birth();
                if (child != null) {
                    population.add(child);
                }
            }

        }

        for (int i = 0; i < food.size(); i++) {
            g.setColor(Color.GREEN);
            PVector f = food.get(i);

            //System.out.println("DRAWING FOOD @" + f.x +"||"+ f.y);
            g.fillOval((int) f.x, (int) f.y, 10, 10);
            g.setColor(Color.BLACK);
        }

        for (int i = 0; i < poison.size(); i++) {
            g.setColor(Color.RED);
            PVector p = poison.get(i);
            //System.out.println("DRAWING POISON @" + p.x +"||"+ p.y);
            g.fillOval((int) p.x, (int) p.y, 10, 10);
            g.setColor(Color.BLACK);
        }
    }


    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        draw((Graphics2D) graphics);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == timer) {
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        Point p = mouseEvent.getPoint();
        population.add(new Vehicle(p.x, p.y));
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}