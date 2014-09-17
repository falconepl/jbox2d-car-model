import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            log.warn("Could not set the look and feel to Nimbus.");
        }

        TestbedModel model = new TestbedModel();
        TestbedPanel panel = new TestPanelJ2D(model);
        CustomTestList.populateModel(model);
        TestList.populateModel(model);
        JFrame testbed = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        testbed.setVisible(true);
        testbed.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
