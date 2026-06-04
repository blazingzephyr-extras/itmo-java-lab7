package se.ifmo.blazingzephyr.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.i18n.LocaleManager;
import se.ifmo.blazingzephyr.model.*;

public class OrganizationDialogController {

    @FXML private TextField nameField, xField, yField;
    @FXML private TextField annualTurnoverField, fullNameField;
    @FXML private TextField addressField, zipCodeField;
    @FXML private ChoiceBox<OrganizationType> orgTypeBox;

    @FXML private Label nameLabel, xLabel, yLabel;
    @FXML private Label annualTurnoverLabel, fullNameLabel;
    @FXML private Label orgTypeLabel, addressLabel, zipCodeLabel;
    @FXML private Button okButton, cancelButton;

    private OrganizationData result = null;
    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML
    public void initialize() {
        orgTypeBox.getItems().addAll(OrganizationType.values());

        // Подписываемся на смену локали
        lm.bundleProperty().addListener((obs, o, n) -> applyLocale());
        applyLocale();
    }

    // Обновляет все тексты полей согласно текущей локали.
    private void applyLocale() {
        nameLabel.setText(lm.get("dialog.name"));
        xLabel.setText(lm.get("dialog.x"));
        yLabel.setText(lm.get("dialog.y"));
        annualTurnoverLabel.setText(lm.get("dialog.annualTurnover"));
        fullNameLabel.setText(lm.get("dialog.fullName"));
        orgTypeLabel.setText(lm.get("dialog.orgType"));
        addressLabel.setText(lm.get("dialog.address"));
        zipCodeLabel.setText(lm.get("dialog.zipCode"));
        okButton.setText(lm.get("dialog.ok"));
        cancelButton.setText(lm.get("dialog.cancel"));
    }

    @FXML
    public void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    public void handleOk() {
        if (nameField.getText().trim().isEmpty()) {
            App.showPopup(lm.get("error.name.empty"));
            return;
        }

        double x;
        try {
            x = Double.parseDouble(xField.getText());
            if (x > 213.0) {
                App.showPopup(lm.get("error.x.max"));
                return;
            }
        } catch (NumberFormatException e) {
            App.showPopup(lm.get("error.x.notNumber"));
            return;
        }

        String street = addressField.getText().trim();
        if (street == null) {
            App.showPopup(lm.get("error.street.null"));
            return;
        }

        float y;
        try {
            y = Float.parseFloat(yField.getText());
        } catch (NumberFormatException e) {
            App.showPopup(lm.get("error.y.notNumber"));
            return;
        }

        result = new OrganizationData(
            nameField.getText().trim(),
            new Coordinates().setX(x).setY(y),
            Double.parseDouble(annualTurnoverField.getText()),
            fullNameField.getText().trim(),
            orgTypeBox.getValue(),
            new Address().setStreet(street).setZipCode(zipCodeField.getText().trim())
        );

        ((Stage) nameField.getScene().getWindow()).close();
    }

    /** Заполняет окно значениями существующего объекта. */
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
