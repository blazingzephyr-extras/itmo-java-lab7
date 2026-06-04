package se.ifmo.blazingzephyr.controllers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.ClientCommands;
import se.ifmo.blazingzephyr.CommandUtility;
import se.ifmo.blazingzephyr.ValidationResult;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;
import se.ifmo.blazingzephyr.model.OrganizationWithId;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

public class PrimaryController {

    @FXML private Label userLogin;
    @FXML private TableView<OrganizationWithId> tableView;
    @FXML private TableColumn<OrganizationWithId, Long> idColumn;
    @FXML private TableColumn<OrganizationWithId, String> nameColumn;
    @FXML private TableColumn<OrganizationWithId, Double> xColumn;
    @FXML private TableColumn<OrganizationWithId, Float> yColumn;
    @FXML private TableColumn<OrganizationWithId, Double> annualTurnoverColumn;
    @FXML private TableColumn<OrganizationWithId, String> fullNameColumn;
    @FXML private TableColumn<OrganizationWithId, OrganizationType> orgTypeColumn;
    @FXML private TableColumn<OrganizationWithId, String> addressColumn;
    @FXML private TableColumn<OrganizationWithId, String> zipCodeColumn;
    @FXML private TableColumn<OrganizationWithId, String> ownerColumn;
    @FXML private TextField console;
    @FXML private TextArea out;

    private CommandUtility commands;
    private ArrayList<Request> history;

    public void setLogin(String login) {
        this.userLogin.setText(login);
    }

    public void loadTable() {
        try {
            Request request = new Request(CommandType.SHOW);
            Response response = App.sendRequest(request);
            tableView.setItems(FXCollections.observableArrayList(response.getData()));
        } catch (Exception e) {
            App.showPopup("Ошибка при загрузке данных с сервера. " + e.getLocalizedMessage());
        }
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getName()));
        xColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getCoordinates().getX()));
        yColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getCoordinates().getY()));
        annualTurnoverColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getAnnualTurnover()));
        fullNameColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getFullName()));
        orgTypeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getData().getOrganizationType()));
        addressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().getOfficialAddress().getStreet()));
        zipCodeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().getOfficialAddress().getZipCode()));
        ownerColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOwner()));

        this.commands = new CommandUtility();
        this.history = new ArrayList<>();
    }

    @FXML
    public void send() {

        String input = console.getText().trim();
        if (input.isEmpty()) return;
        console.clear();

        ValidationResult validation = commands.validate(input);
        if (validation.isError()) {
            appendOutput("Ошибка: " + validation.error().get().getMessage());
            return;
        }

        Request request = validation.request().get();

        if (request.getCommandType() == CommandType.HISTORY) {
            appendOutput(ClientCommands.printHistory(history));
            return;
        }

        if (request.getCommandType() == CommandType.HELP) {
            appendOutput(ClientCommands.printHelp(request.getPayload(), commands.getCommands()));
            return;
        }

        if (request.getCommandType() == CommandType.EXIT) {
            return;
        }

        if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
            String result = ClientCommands.executeScript(request, commands);
            appendOutput(result);
            history.add(request);
            loadTable(); // обновить таблицу после скрипта
            return;
        }

        try {
            Response response = App.sendRequest(request);
            history.add(request);
            appendOutput(new String(response.getMessage()));
            loadTable();
        } catch (Exception e) {
            appendOutput("Ошибка: " + e.getLocalizedMessage());
        }
    }

    private void appendOutput(String message) {
        out.appendText(message + "\n\n");
    }
}
