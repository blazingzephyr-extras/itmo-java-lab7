package se.ifmo.blazingzephyr.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.controllers.OrganizationDialogController;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationWithId;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;

public class EditDialogUtility {
    
    public static Response openEditDialog(OrganizationWithId org) throws Exception {

        FXMLLoader loader = new FXMLLoader(App.class.getResource("dialog.fxml"));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(loader.load()));

        // Передаём существующие данные в диалог
        OrganizationDialogController controller = loader.getController();
        controller.prefill(org.getData());

        dialog.showAndWait();

        OrganizationData result = controller.getResult();
        if (result != null) {
            Request request = new Request(
                CommandType.UPDATE,
                new CommandPayload.WithIdAndOrganization(org.getId(), result));

            Response response = App.sendRequest(request);
            return response;
        }

        return null;
    }
}
