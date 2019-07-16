import Actions.ApplicationStartedAction;
import java.awt.event.ActionEvent;

public class Main {

    public static void main(String[] args) {

        ApplicationStartedAction  applicationStartedAction = new ApplicationStartedAction();
        applicationStartedAction.actionPerformed(new ActionEvent(null, 0, null));
    }
}
