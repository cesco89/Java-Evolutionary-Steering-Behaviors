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
    private float health = 5;
    private double eatPrecision = 10;
    private int x;
    private int y;

    //private double flockingRange = GamePanel.radius *3;

    //set to false to remove food/poison attraction circles
    private boolean debug = false;
    private boolean debugFlock = true;

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
            this.dna = new double[5];
            this.dna[0] = randomInRange(-maxf, maxf);
            this.dna[1] = randomInRange(-maxf, maxf);
            this.dna[2] = randomInRange(5, 100);
            this.dna[3] = randomInRange(5, 100);
            this.dna[4] = randomInRange(radius, radius*2);
        }

        this.health = 1;

    }


    void flock(ArrayList<Vehicle> vehicles) {
        PVector sep = separate(vehicles);   // Separation
        PVector ali = align(vehicles);      // Alignment
        PVector coh = cohesion(vehicles);   // Cohesion
        // Arbitrarily weight these forces
        sep.mult(1.5f);
        ali.mult(1.0f);
        coh.mult(1.0f);
        // Add the force vectors to acceleration
        applyForce(sep);
        applyForce(ali);
        applyForce(coh);
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

    public void eat(ArrayList<Vehicle> population, ArrayList<PVector> list, int index, double[] nutrition) {
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
                if (index == 0) {
                    this.dna[index] -= 0.01;
                } else if (index == 1) {
                    this.dna[index] += 0.01;
                }
                checkFlocked(population, index, nutrition);
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

    private void checkFlocked(ArrayList<Vehicle> population, int index, double[] nutrition) {
        for (int i = 0; i < population.size(); i++) {
            Vehicle v = population.get(i);
            float d = PVector.dist(v.position, this.position);
            if (d < this.dna[4]) {
                if (index == 0) {
                    v.dna[index] -= 0.01;
                } else if (index == 1) {
                    v.dna[index] += 0.01;
                }
                if(this.dna[4] < radius*3) {
                    this.dna[4] += 0.1;
                }
                v.health += nutrition[index];
            }
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

    void run(ArrayList<Vehicle> vehicles, int width, int height) {
        flock(vehicles);
        update();
        boundaries(width, height);
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

            int index1 = 2; //default 2
            int index2 = 3; //default 3
            int r1 = (int) this.dna[index1] * 2;
            int r2 = (int) this.dna[index2] * 2;
            int x1 = (int) this.position.x - ((int) this.dna[index1]) + (radius / 2);
            int y1 = (int) this.position.y - ((int) this.dna[index1]) + (radius / 2);
            int x2 = (int) this.position.x - ((int) this.dna[index2]) + (radius / 2);
            int y2 = (int) this.position.y - ((int) this.dna[index2]) + (radius / 2);


            g.setColor(Color.GREEN);
            g.drawOval(x1, y1, r1, r1);

            g.setColor(Color.RED);
            g.drawOval(x2, y2, r2, r2);

            g.setColor(Color.BLACK);

        } else if (debugFlock) {
            int x1 = (int) this.position.x - ((int) this.dna[4] ) + (radius/2) + ((int) this.dna[4]/2);
            int y1 = (int) this.position.y - ((int) this.dna[4] ) + (radius/2) + ((int) this.dna[4]/2);
            g.setColor(Color.GREEN);
            g.drawOval(x1, y1, (int) this.dna[4], (int) this.dna[4]);
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

    PVector separate(ArrayList<Vehicle> vehicles) {
        float desiredseparation = 25.0f;
        PVector steer = new PVector(0, 0, 0);
        int count = 0;
        // For every boid in the system, check if it's too close
        for (Vehicle other : vehicles) {
            float d = PVector.dist(position, other.position);
            // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
            if ((d > 0) && (d < desiredseparation)) {
                // Calculate vector pointing away from neighbor
                PVector diff = PVector.sub(position, other.position);
                diff.normalize();
                diff.div(d);        // Weight by distance
                steer.add(diff);
                count++;            // Keep track of how many
            }
        }
        // Average -- divide by how many
        if (count > 0) {
            steer.div((float) count);
        }

        // As long as the vector is greater than 0
        if (steer.mag() > 0) {
            // Implement Reynolds: Steering = Desired - Velocity
            steer.normalize();
            steer.mult(maxspeed);
            steer.sub(velocity);
            steer.limit((float) maxforce);
        }
        return steer;
    }

    private PVector align(ArrayList<Vehicle> vehicles) {
        float neighbordist = 50;
        PVector sum = new PVector(0, 0);
        int count = 0;
        for (Vehicle other : vehicles) {
            float d = PVector.dist(position, other.position);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.velocity);
                count++;
            }
        }
        if (count > 0) {
            sum.div((float) count);
            sum.normalize();
            sum.mult(maxspeed);
            PVector steer = PVector.sub(sum, velocity);
            steer.limit((float) maxforce);
            return steer;
        } else {
            return new PVector(0, 0);
        }
    }

    private PVector cohesion(ArrayList<Vehicle> vehicles) {
        float neighbordist = 50;
        PVector sum = new PVector(0, 0);   // Start with empty vector to accumulate all positions
        int count = 0;
        for (Vehicle other : vehicles) {
            float d = PVector.dist(position, other.position);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.position); // Add position
                count++;
            }
        }
        if (count > 0) {
            sum.div(count);
            return seekNei(sum);  // Steer towards the position
        } else {
            return new PVector(0, 0);
        }
    }

    private PVector seekNei(PVector target) {
        PVector desired = PVector.sub(target, position);  // A vector pointing from the position to the target
        // Normalize desired and scale to maximum speed
        desired.normalize();
        desired.mult(maxspeed);
        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity);
        steer.limit((float) maxforce);  // Limit to maximum steering force
        return steer;
    }

}
