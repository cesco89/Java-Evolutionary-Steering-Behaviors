import java.awt.*;

public class Utils {

    static public final float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    static public int lerpColor(int c1, int c2, float amt) {
        if (amt < 0) amt = 0;
        if (amt > 1) amt = 1;

        float a1 = ((c1 >> 24) & 0xff);
        float r1 = (c1 >> 16) & 0xff;
        float g1 = (c1 >> 8) & 0xff;
        float b1 = c1 & 0xff;
        float a2 = (c2 >> 24) & 0xff;
        float r2 = (c2 >> 16) & 0xff;
        float g2 = (c2 >> 8) & 0xff;
        float b2 = c2 & 0xff;

        return ((Math.round(a1 + (a2 - a1) * amt) << 24) |
                (Math.round(r1 + (r2 - r1) * amt) << 16) |
                (Math.round(g1 + (g2 - g1) * amt) << 8) |
                (Math.round(b1 + (b2 - b1) * amt)));


    }

}
