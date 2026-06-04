package se.ifmo.blazingzephyr.controllers;

import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.ClientCommands;
import se.ifmo.blazingzephyr.CommandUtility;
import se.ifmo.blazingzephyr.ValidationResult;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;
import se.ifmo.blazingzephyr.model.OrganizationWithId;
import se.ifmo.blazingzephyr.networking.CommandPayload;
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
            redrawCanvas(response.getData());
            startPolling();
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

        canvas.setOnMouseClicked(event -> {
            double mx = event.getX();
            double my = event.getY();

            for (OrganizationWithId org : tableView.getItems()) {
                double x = org.getData().getCoordinates().getX() % canvas.getWidth();
                double y = org.getData().getCoordinates().getY() % canvas.getHeight();
                double size = 20;

                // Проверяем попадание в круг
                if (mx >= x && mx <= x + size && my >= y && my <= y + size) {
                    if (event.getClickCount() == 2) {
                        openEditDialog(org); // двойной клик — редактировать
                    } else {
                        App.showPopup(String.format("ID: %d\nНазвание: %s\nВладелец: %s",
                            org.getId(), org.getData().getName(), org.getOwner()));
                    }
                    break;
                }
            }
        });

        tableView.setRowFactory(tv -> {
            TableRow<OrganizationWithId> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Удалить");

            deleteItem.setOnAction(e -> {
                OrganizationWithId org = row.getItem();
                try {
                    Request request = new Request(CommandType.REMOVE_BY_ID, new CommandPayload.WithId(org.getId()));
                    Response response = App.sendRequest(request);
                    appendOutput(new String(response.getMessage()));
                    loadTable();
                } catch (Exception ex) {
                    appendOutput("Ошибка: " + ex.getLocalizedMessage());
                }
            });

            contextMenu.getItems().add(deleteItem);
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            MenuItem editItem = new MenuItem("Редактировать");
            editItem.setOnAction(e -> {
                OrganizationWithId org = row.getItem();
                openEditDialog(org);
            });
            contextMenu.getItems().addAll(editItem, deleteItem);

            return row;
        });
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

            // Анимировать только если команда добавляет объект
            if (request.getCommandType() == CommandType.ADD 
                    || request.getCommandType() == CommandType.ADD_IF_MIN) {
                // берём последний добавленный объект из таблицы
                List<OrganizationWithId> items = tableView.getItems();
                if (!items.isEmpty()) {
                    animateOrg(items.get(items.size() - 1));
                }
            }
        } catch (Exception e) {
            appendOutput("Ошибка: " + e.getLocalizedMessage());
        }
    }

    private void appendOutput(String message) {
        out.appendText(message + "\n\n");
    }

    //

    @FXML private Canvas canvas;

    // Цвета для разных пользователей
    private final Map<String, Color> userColors = new HashMap<>();
    private final List<Color> palette = List.of(
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PINK
    );

    private Color getColorForUser(String owner) {
        return userColors.computeIfAbsent(owner, k -> palette.get(userColors.size() % palette.size()));
    }

    public void redrawCanvas(List<OrganizationWithId> orgs) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (OrganizationWithId org : orgs) {
            double x = org.getData().getCoordinates().getX();
            double y = org.getData().getCoordinates().getY();
            double size = org.getData().getAnnualTurnover() != null
                ? Math.min(org.getData().getAnnualTurnover() / 1000, 50) + 10
                : 20;

            Color color = getColorForUser(org.getOwner());
            gc.setFill(color);

            // Рисуем круг
            gc.fillOval(x % canvas.getWidth(), y % canvas.getHeight(), size, size);

            // Подпись
            gc.setFill(Color.BLACK);
            gc.fillText(org.getData().getName(), x % canvas.getWidth(), y % canvas.getHeight() - 5);
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
                    redrawCanvas(newData);
                });
            } catch (Exception ex) {
                // тихо игнорируем ошибки поллинга
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void animateOrg(OrganizationWithId org) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double x = org.getData().getCoordinates().getX() % canvas.getWidth();
        double y = org.getData().getCoordinates().getY() % canvas.getHeight();
        Color color = getColorForUser(org.getOwner());

        // Анимация — круг увеличивается от 0 до нужного размера
        double targetSize = 20;
        AnimationTimer timer = new AnimationTimer() {
            double size = 0;

            @Override
            public void handle(long now) {
                size += 0.05;
                gc.setFill(color);
                gc.fillOval(x, y, size, size);
                if (size >= targetSize) stop();
            }
        };
        timer.start();
    }

    private void openEditDialog(OrganizationWithId org) {
        try {
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
                Request request = new Request(CommandType.UPDATE,
                    new CommandPayload.WithIdAndOrganization(org.getId(), result));
                Response response = App.sendRequest(request);
                appendOutput(new String(response.getMessage()));
                loadTable();
            }
        } catch (Exception e) {
            appendOutput("Ошибка: " + e.getLocalizedMessage());
        }
    }
}
