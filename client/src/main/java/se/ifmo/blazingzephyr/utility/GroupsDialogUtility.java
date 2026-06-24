package se.ifmo.blazingzephyr.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.controllers.GroupsDialogController;

public class GroupsDialogUtility {
    
    public static void openGroupsDialog() throws Exception {

        FXMLLoader loader = new FXMLLoader(App.class.getResource("groups.fxml"));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(loader.load()));

        GroupsDialogController controller = loader.getController();
        controller.loadInitialData();

        dialog.showAndWait();
    }
}
