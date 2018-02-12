import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main (String... args){
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("GAME OF LIFE");
            GamePanel panel = new GamePanel();
            panel.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
