package se.ifmo.blazingzephyr.controllers;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.ClientCommands;
import se.ifmo.blazingzephyr.CommandUtility;
import se.ifmo.blazingzephyr.ValidationResult;
import se.ifmo.blazingzephyr.model.*;
import se.ifmo.blazingzephyr.networking.*;
import se.ifmo.blazingzephyr.utility.CanvasUtility;
import se.ifmo.blazingzephyr.utility.EditDialogUtility;
import se.ifmo.blazingzephyr.utility.TableRowFactory;

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
    
    // Канва.
    @FXML private Canvas canvas;

    // Данные из предыдущего App.
    private CommandUtility commands;
    private ArrayList<Request> history;

    // Высвечивает сбоку, какой пользователь нынешний.
    public void setLogin(String login) {
        this.userLogin.setText(login);
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
        canvas.setOnMouseClicked(
            event -> CanvasUtility.onMouseClicked(
                event,
                canvas,
                tableView,

                // На клике.
                org -> App.showPopup(
                    String.format(
                        "ID: %d\nНазвание: %s\nВладелец: %s",
                        org.getId(),
                        org.getData().getName(),
                        org.getOwner()
                    )
                ),

                // На двойном клике.
                org -> {
                    try {
                        Response response = EditDialogUtility.openEditDialog(org);
                        out.appendText(new String(response.getMessage()) + "\n\n");
                        loadTable();
                    } catch (Exception e) {
                        out.appendText("Ошибка: " + e.getLocalizedMessage() + "\n\n");
                    }
                }
            )
        );
        
        tableView.setRowFactory(
            tv -> TableRowFactory.factory(
                tv,
                // При удалении
                row -> {
                    OrganizationWithId org = row.getItem();
                    try {
                        Request request = new Request(CommandType.REMOVE_BY_ID, new CommandPayload.WithId(org.getId()));
                        Response response = App.sendRequest(request);
                        out.appendText(new String(response.getMessage()) + "\n\n");
                        loadTable();
                    } catch (Exception ex) {
                        out.appendText("Ошибка: " + ex.getLocalizedMessage() + "\n\n");
                    }
                },
                // При редактировании
                row -> {
                    OrganizationWithId org = row.getItem();
                    try {
                        Response response = EditDialogUtility.openEditDialog(org);
                        out.appendText(new String(response.getMessage()) + "\n\n");
                        loadTable();
                    } catch (Exception e) {
                        out.appendText("Ошибка: " + e.getLocalizedMessage() + "\n\n");
                    }
                }
            )
        );
    }

    // Загружает таблицу при открытии.
    public void loadTable() {
        try {
            Request request = new Request(CommandType.SHOW);
            Response response = App.sendRequest(request);
            tableView.setItems(FXCollections.observableArrayList(response.getData()));
            CanvasUtility.redrawCanvas(canvas, response.getData());
            startPolling();
        } catch (Exception e) {
            App.showPopup("Ошибка при загрузке данных с сервера. " + e.getLocalizedMessage());
        }
    }

    @FXML
    public void add() {
        
        String input = "add";
        sendReq(input);
    }

    @FXML
    public void info() {
        
        String input = "info";
        sendReq(input);
    }

    @FXML
    public void clear() {
        
        String input = "clear";
        sendReq(input);
    }
    
    // Отправляет реквест.
    @FXML
    public void send() {

        String input = console.getText().trim();
        if (input.isEmpty()) return;
        console.clear();
        sendReq(input);
    }

    // Отправляет реквест.
    public void sendReq(String input) {

        ValidationResult validation = commands.validate(input);
        if (validation.isError()) {
            out.appendText("Ошибка: " + validation.error().get().getMessage() + "\n\n");
            return;
        }

        Request request = validation.request().get();

        if (request.getCommandType() == CommandType.HISTORY) {
            out.appendText(ClientCommands.printHistory(history) + "\n\n");
            return;
        }

        if (request.getCommandType() == CommandType.HELP) {
            out.appendText(ClientCommands.printHelp(request.getPayload(), commands.getCommands()) + "\n\n");
            return;
        }

        if (request.getCommandType() == CommandType.EXIT) {
            return;
        }

        if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
            String result = ClientCommands.executeScript(request, commands);
            out.appendText(result + "\n\n");
            history.add(request);
            loadTable(); // обновить таблицу после скрипта
            return;
        }

        try {
            Response response = App.sendRequest(request);
            history.add(request);
            out.appendText(new String(response.getMessage()) + "\n\n");
            loadTable();

            // Анимировать только если команда добавляет объект
            if (request.getCommandType() == CommandType.ADD 
                    || request.getCommandType() == CommandType.ADD_IF_MIN) {
                // берём последний добавленный объект из таблицы
                List<OrganizationWithId> items = tableView.getItems();
                if (!items.isEmpty()) {
                    CanvasUtility.animateOrg(canvas, items.get(items.size() - 1));
                }
            }
        } catch (Exception e) {
            out.appendText("Ошибка: " + e.getLocalizedMessage() + "\n\n");
        }
    }
    
    private void startPolling() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            try {
                Request request = new Request(CommandType.SHOW);
                Response response = App.sendRequest(request);
                List<OrganizationWithId> newData = response.getData();

                Platform.runLater(() -> {
                    tableView.setItems(FXCollections.observableArrayList(newData));
                    CanvasUtility.redrawCanvas(canvas, newData);
                });
            } catch (Exception ex) {
                // тихо игнорируем ошибки поллинга
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
