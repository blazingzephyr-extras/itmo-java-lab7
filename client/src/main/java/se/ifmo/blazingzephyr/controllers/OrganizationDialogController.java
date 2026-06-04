package se.ifmo.blazingzephyr.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    private void handleOk() {
        // валидация
        if (nameField.getText().trim().isEmpty()) {
            // показать ошибку
            return;
        }

        result = new OrganizationData(
            nameField.getText().trim(),
            new Coordinates()
                .setX(Double.parseDouble(xField.getText()))
                .setY(Float.parseFloat(yField.getText())),
            Double.parseDouble(annualTurnoverField.getText()),
            fullNameField.getText().trim(),
            orgTypeBox.getValue(),
            new Address()
                .setStreet(addressField.getText().trim())
                .setZipCode(zipCodeField.getText().trim())
        );

        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

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