package se.ifmo.blazingzephyr.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.model.*;

public class OrganizationDialogController {

    @FXML private TextField nameField, xField, yField;
    @FXML private TextField annualTurnoverField, fullNameField;
    @FXML private TextField addressField, zipCodeField;
    @FXML private ChoiceBox<OrganizationType> orgTypeBox;
    
    private OrganizationData result = null;

    @FXML
    public void initialize() {
        orgTypeBox.getItems().addAll(OrganizationType.values());
    }

    @FXML
    public void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    public void handleOk() {
        if (nameField.getText().trim().isEmpty()) {
            App.showPopup("Некорректные данные: названием не может быть пустым.");
            return;
        }

        double x;
        try {
            x = Double.parseDouble(xField.getText());
            if (x > 213.0) {
                App.showPopup("Максимальное значение 'x' - 213.");
                return;
            }
        }
        catch (NumberFormatException e) {
            App.showPopup("x должно быть числом.");
            return;
        }

        String street = addressField.getText().trim();
        if (street == null) {
            App.showPopup("Значение 'street' не может быть null.");
            return;
        }

        
        float y;
        try {
            y = Float.parseFloat(yField.getText());;
        }
        catch (NumberFormatException e) {
            App.showPopup("y должно быть числом.");
            return;
        }

        result = new OrganizationData(
            nameField.getText().trim(),
            new Coordinates()
                .setX(x)
                .setY(y),
            Double.parseDouble(annualTurnoverField.getText()),
            fullNameField.getText().trim(),
            orgTypeBox.getValue(),
            new Address()
                .setStreet(street)
                .setZipCode(zipCodeField.getText().trim())
        );

        ((Stage) nameField.getScene().getWindow()).close();
    }

    // Заполняет окно значениями существующего объекта.
    public void prefill(OrganizationData data) {
        nameField.setText(data.getName());
        xField.setText(String.valueOf(data.getCoordinates().getX()));
        yField.setText(String.valueOf(data.getCoordinates().getY()));
        annualTurnoverField.setText(String.valueOf(data.getAnnualTurnover()));
        fullNameField.setText(data.getFullName() != null ? data.getFullName() : "");
        orgTypeBox.setValue(data.getOrganizationType());
        if (data.getOfficialAddress() != null) {
            addressField.setText(data.getOfficialAddress().getStreet() != null
                ? data.getOfficialAddress().getStreet() : "");
            zipCodeField.setText(data.getOfficialAddress().getZipCode() != null
                ? data.getOfficialAddress().getZipCode() : "");
        }
    }

    public OrganizationData getResult() { return result; }
}
