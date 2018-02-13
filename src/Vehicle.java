import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.awt.*;
import java.util.ArrayList;

public class Vehicle {

    private PVector acceleration;
    private PVector velocity;
    private PVector position;
    private int radius = GamePanel.radius;
    private double maxforce = 0.5;
    private float maxspeed = 3;
    private double[] dna;
    private float health = 3;
    private double eatPrecision = 5;
    private int x;
    private int y;

    //set to false to remove food/poison attraction circles
    private boolean debug = true;

    Vehicle(int x, int y) {
        this(x, y, null);
    }

    private Vehicle(int x, int y, double[] dna) {
        this.dna = new double[5];
        this.x = x;
        this.y = y;
        this.acceleration = new PVector();
        this.velocity = PVector.random2DVector();
        this.position = new PVector(x, y);
        this.maxforce = 0.5;
        this.velocity.setMag((float) this.maxspeed);

        if (dna != null) {
            // DNA
            // 0: Attraction/Repulsion to food
            // 1: Attraction/Repulsion to poison
            // 2: Radius to sense food
            // 3: Radius to sense poison
            for (int i = 0; i < dna.length; i++) {
                if (Math.random() < 0.1) {
                    if (i < 2) {
                        this.dna[i] = dna[i] + randomInRange(-0.2, 0.2);
                    } else {
                        this.dna[i] = dna[i] + randomInRange(-10, 10);
                    }
                } else {
                    this.dna[i] = dna[i];
                }
            }

        } else {
            int maxf = 3;
            this.dna = new double[4];
            this.dna[0] = randomInRange(-maxf, maxf);
            this.dna[1] = randomInRange(-maxf, maxf);
            this.dna[2] = randomInRange(5, 100);
            this.dna[3] = randomInRange(5, 100);
        }

        this.health = 1;

    }

    public void update() {
        this.velocity.add(this.acceleration);
        this.velocity.limit(this.maxspeed);
        this.position.add(this.velocity);
        this.acceleration.mult(0);
        this.health -= 0.001;
        //System.out.println("HEALTH: "+health);
    }

    public boolean dead() {
        return (this.health < 0);
    }

    public Vehicle birth() {
        if (Math.random() < 0.0008) {
            Vehicle child = new Vehicle(this.x, this.y, this.dna);
            child.health += 0.01;
            return child;
        }
        return null;
    }

    public void eat(ArrayList<PVector> list, int index, double[] nutrition) {
        PVector closest = null;
        Double closestD = Double.POSITIVE_INFINITY;
        for (int i = list.size() - 1; i >= 0; i--) {
            float d = PVector.dist(list.get(i), this.position);
            if (d < this.dna[2 + index] && d < closestD) {
                closestD = (double) d;
                closest = list.get(i);
            }
            if (d < (eatPrecision)) {
                //System.out.println("EATING SOMETHING");
                list.remove(i);
                this.health += nutrition[index];
            }
        }

        if (closest != null) {
            //System.out.println("FOUND SOMETHING");
            PVector sk = this.seek(closest, index);
            sk.mult((float) this.dna[index]);
            sk.limit((float) this.maxforce);
            //System.out.println("STEERING!");
            this.applyForce(sk);
        }
    }

    public PVector seek(PVector target, int index) {
        //System.out.println("SEARCHING...");
        PVector desired = PVector.sub(target, this.position);
        double d = desired.mag();

        desired.setMag(this.maxspeed);
        PVector steer = PVector.sub(desired, this.velocity);

        return steer;
    }

    public void applyForce(PVector force) {
        this.acceleration.add(force);
    }


    private double randomInRange(double Min, double Max) {
        return (Min + (Math.random() * ((Max - Min) + 1)));
    }


    public void display(ArrayList<Vehicle> population, int position, Graphics2D g) {
        int blue = Color.GREEN.getRGB();
        int red = Color.RED.getRGB();
        int col = Utils.lerpColor(red, blue, this.health);

        double tetha = this.velocity.heading2D() + (Math.PI / 2);
        //g.translate((int)this.position.x, (int)this.position.y);

        g.setColor(new Color(col));
        g.fillOval((int) this.position.x, (int) this.position.y, this.radius, this.radius);
        g.setColor(Color.BLACK);

        if (debug) {

            // Circle and line for food
            g.setColor(Color.GREEN);
            g.drawOval((int) this.position.x - ((int) this.dna[2]) + radius/2, (int) this.position.y  - ((int) this.dna[2]) + radius/2, (int) this.dna[2]*2, (int) this.dna[2]*2);

            g.setColor(Color.RED);
            // Circle and line for poison
            g.drawOval((int) this.position.x - ((int) this.dna[3]) + radius/2, (int) this.position.y  - ((int) this.dna[3]) + radius/2, (int) this.dna[3]*2, (int) this.dna[3]*2);

            g.setColor(Color.BLACK);

        }

    }

    public void boundaries(int width, int height) {
        int d = 10;
        PVector desired = null;
        if (this.position.x < d) {
            desired = new PVector(this.maxspeed, this.velocity.y);
        } else if (this.position.x > width - d) {
            desired = new PVector(-this.maxspeed, -this.velocity.y);
        }

        if (this.position.y < d) {
            desired = new PVector(this.velocity.x, this.maxspeed);
        } else if (this.position.y > height - d) {
            desired = new PVector(this.velocity.x, -this.maxspeed);
        }

        if (desired != null) {
            desired.setMag(this.maxspeed);
            PVector steer = PVector.sub(desired, this.velocity);
            steer.limit((float) this.maxforce);
            this.applyForce(steer);
        }


    }

}
