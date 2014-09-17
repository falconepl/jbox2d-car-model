import org.jbox2d.testbed.framework.TestbedModel;

public class CustomTestList {

    public static void populateModel(TestbedModel model) {
        model.addCategory("Custom");
        model.addTest(new TopDownCar());
    }

}
