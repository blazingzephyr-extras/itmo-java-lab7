package se.ifmo.blazingzephyr.controllers;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.ClientCommands;
import se.ifmo.blazingzephyr.CommandUtility;
import se.ifmo.blazingzephyr.ValidationResult;
import se.ifmo.blazingzephyr.i18n.LocaleManager;
import se.ifmo.blazingzephyr.model.*;
import se.ifmo.blazingzephyr.networking.*;
import se.ifmo.blazingzephyr.utility.CanvasUtility;
import se.ifmo.blazingzephyr.utility.EditDialogUtility;
import se.ifmo.blazingzephyr.utility.TableRowFactory;

public class PrimaryController {

    // ---------- FXML ----------
    @FXML private Label userLogin;
    @FXML private Label userLoginLabel;      // метка «Пользователь:»

    @FXML private TableView<OrganizationWithId> tableView;
    @FXML private TableColumn<OrganizationWithId, Long>             idColumn;
    @FXML private TableColumn<OrganizationWithId, String>           nameColumn;
    @FXML private TableColumn<OrganizationWithId, Double>           xColumn;
    @FXML private TableColumn<OrganizationWithId, Float>            yColumn;
    @FXML private TableColumn<OrganizationWithId, Double>           annualTurnoverColumn;
    @FXML private TableColumn<OrganizationWithId, String>           fullNameColumn;
    @FXML private TableColumn<OrganizationWithId, OrganizationType> orgTypeColumn;
    @FXML private TableColumn<OrganizationWithId, String>           addressColumn;
    @FXML private TableColumn<OrganizationWithId, String>           zipCodeColumn;
    @FXML private TableColumn<OrganizationWithId, String>           ownerColumn;

    @FXML private TextField console;
    @FXML private TextArea  out;
    @FXML private Canvas    canvas;

    // ---------- Фильтр / сортировка ----------
    /** Столбцы для фильтрации, ключи совпадают с col.* в бандле */
    private enum FilterColumn {
        ID, NAME, X, Y, ANNUAL_TURNOVER, FULL_NAME, ORG_TYPE, ADDRESS, ZIP_CODE, OWNER
    }

    @FXML private ChoiceBox<FilterColumn> filterColumnBox;
    @FXML private TextField               filterField;
    @FXML private Label                   filterLabel;

    /** Полный список, полученный с сервера (без фильтра) */
    private List<OrganizationWithId> allData = new ArrayList<>();

    /**
     * Единственный ObservableList, который навсегда привязан к tableView.
     * Мы никогда не делаем setItems() повторно — только меняем содержимое этого списка.
     * Это ключевое: TableView сбрасывает sortOrder именно при замене самого списка.
     */
    private final javafx.collections.ObservableList<OrganizationWithId> tableData =
        FXCollections.observableArrayList();

    // ---------- Прочие поля ----------
    private CommandUtility       commands;
    private ArrayList<Request>   history;
    private final LocaleManager  lm = LocaleManager.getInstance();

    /** Единственный экземпляр таймера поллинга. Не пересоздаётся при loadTable(). */
    private Timeline pollingTimeline;

    // ---------- Инициализация ----------

    public void setLogin(String login) {
        this.userLogin.setText(login);
    }

    @FXML
    public void initialize() {
        // Cell factories
        idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().getName()));
        xColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getData().getCoordinates().getX()));
        yColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getData().getCoordinates().getY()));
        annualTurnoverColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getData().getAnnualTurnover()));
        fullNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().getFullName()));
        orgTypeColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getData().getOrganizationType()));
        addressColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().getOfficialAddress().getStreet()));
        zipCodeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().getOfficialAddress().getZipCode()));
        ownerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwner()));

        // Привязываем ОДИН РАЗ — больше setItems() нигде не вызываем.
        // TableView сбрасывает sortOrder при каждой замене списка,
        // поэтому мы всегда меняем только содержимое tableData через setAll().
        tableView.setItems(tableData);

        this.commands = new CommandUtility();
        this.history  = new ArrayList<>();

        // ---- Выбор столбца фильтра ----
        filterColumnBox.getItems().addAll(FilterColumn.values());
        filterColumnBox.setValue(FilterColumn.NAME);
        filterColumnBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FilterColumn fc) {
                if (fc == null) return "";
                return switch (fc) {
                    case ID             -> lm.get("col.id");
                    case NAME           -> lm.get("col.name");
                    case X              -> lm.get("col.x");
                    case Y              -> lm.get("col.y");
                    case ANNUAL_TURNOVER-> lm.get("col.annualTurnover");
                    case FULL_NAME      -> lm.get("col.fullName");
                    case ORG_TYPE       -> lm.get("col.orgType");
                    case ADDRESS        -> lm.get("col.address");
                    case ZIP_CODE       -> lm.get("col.zipCode");
                    case OWNER          -> lm.get("col.owner");
                };
            }
            @Override public FilterColumn fromString(String s) { return null; }
        });

        // При изменении текста фильтра — применить
        filterField.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        filterColumnBox.valueProperty().addListener((obs, o, n) -> {
            // Обновляем подсказку
            updateFilterPlaceholder();
            applyFilterAndSort();
        });

        // ---- Сортировка ----
        // Слушаем изменения sortOrder напрямую, а не через setSortPolicy.
        // setSortPolicy вызывается и при setItems() — это и было причиной сброса:
        // поллинг ставил новые данные → срабатывал setSortPolicy → applyFilterAndSort()
        // читал ещё не восстановленный sortOrder → сортировка терялась.
        tableView.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<OrganizationWithId, ?>>) c -> {
            applyFilterAndSort();
        });

        // ---- Локализация ----
        lm.bundleProperty().addListener((obs, o, n) -> applyLocale());
        applyLocale();

        // ---- Canvas ----
        canvas.setOnMouseClicked(event -> CanvasUtility.onMouseClicked(
            event, canvas, tableView,
            org -> App.showPopup(String.format(
                lm.get("popup.id") + "\n" + lm.get("popup.name") + "\n" + lm.get("popup.owner"),
                org.getId(), org.getData().getName(), org.getOwner()
            )),
            org -> {
                try {
                    Response response = EditDialogUtility.openEditDialog(org);
                    if (response != null) out.appendText(new String(response.getMessage()) + "\n\n");
                    loadTable();
                } catch (Exception e) {
                    out.appendText(lm.get("error.prefix") + e.getMessage() + "\n\n");
                }
            }
        ));

        // ---- Строки таблицы ----
        tableView.setRowFactory(tv -> TableRowFactory.factory(
            tv,
            row -> {
                OrganizationWithId org = row.getItem();
                try {
                    Request request = new Request(CommandType.REMOVE_BY_ID, new CommandPayload.WithId(org.getId()));
                    Response response = App.sendRequest(request);
                    String key = response.getMessage();
                    out.appendText(String.format(lm.get(key), response.getArgs().toArray()) + "\n\n");
                    loadTable();
                } catch (Exception ex) {
                    out.appendText(lm.get("error.prefix") + ex.getMessage() + "\n\n");
                }
            },
            row -> {
                OrganizationWithId org = row.getItem();
                try {
                    Response response = EditDialogUtility.openEditDialog(org);
                    String key = response.getMessage();
                    if (response != null) out.appendText(String.format(lm.get(key), response.getArgs().toArray()) + "\n\n");
                    loadTable();
                } catch (Exception e) {
                    out.appendText(lm.get("error.prefix") + e.getMessage() + "\n\n");
                }
            }
        ));
    }

    /** Переключает UI на текущую локаль. */
    private void applyLocale() {
        userLoginLabel.setText(lm.get("primary.user") + ":");
        filterLabel.setText(lm.get("primary.filter") + ":");
        updateFilterPlaceholder();

        // Обновить заголовки столбцов
        idColumn.setText(lm.get("col.id"));
        nameColumn.setText(lm.get("col.name"));
        xColumn.setText(lm.get("col.x"));
        yColumn.setText(lm.get("col.y"));
        annualTurnoverColumn.setText(lm.get("col.annualTurnover"));
        fullNameColumn.setText(lm.get("col.fullName"));
        orgTypeColumn.setText(lm.get("col.orgType"));
        addressColumn.setText(lm.get("col.address"));
        zipCodeColumn.setText(lm.get("col.zipCode"));
        ownerColumn.setText(lm.get("col.owner"));

        // Принудительно обновить ChoiceBox (converter не обновляется сам)
        FilterColumn cur = filterColumnBox.getValue();
        filterColumnBox.setConverter(filterColumnBox.getConverter()); // trigger repaint
        filterColumnBox.setValue(null);
        filterColumnBox.setValue(cur);
    }

    private void updateFilterPlaceholder() {
        filterField.setPromptText(lm.get("primary.filterPlaceholder"));
    }

    // ---------- Загрузка данных ----------

    /**
     * Загружает таблицу с сервера в фоновом потоке (JavaFX Task),
     * не блокируя UI-поток на время сетевого запроса.
     */
    public void loadTable() {
        Task<List<OrganizationWithId>> task = new Task<>() {
            @Override
            protected List<OrganizationWithId> call() throws Exception {
                Request request = new Request(CommandType.SHOW);
                return App.sendRequest(request).getData();
            }
        };

        task.setOnSucceeded(e -> {
            allData = new ArrayList<>(task.getValue());
            applyFilterAndSort();
            startPolling(); // безопасно: повторный вызов — no-op
        });

        task.setOnFailed(e -> App.showPopup(
            lm.get("error.loadData") + task.getException().getLocalizedMessage()
        ));

        new Thread(task, "load-table-thread").start();
    }

    // ---------- Фильтрация и сортировка через Streams API ----------

    /**
     * Фильтрует {@code allData} по введённому тексту и выбранному столбцу,
     * затем сортирует согласно состоянию TableView.sortOrder,
     * и обновляет таблицу и канвас.
     *
     * Весь pipeline реализован через Java Streams API.
     */
    private void applyFilterAndSort() {
        String filterText = filterField.getText() == null
            ? "" : filterField.getText().trim().toLowerCase(Locale.ROOT);

        FilterColumn col = filterColumnBox.getValue();

        List<OrganizationWithId> filtered = allData.stream()
            .filter(org -> {
                if (filterText.isEmpty()) return true;
                String value = extractColumnValue(org, col);
                return value != null && value.toLowerCase(Locale.ROOT).contains(filterText);
            })
            .sorted(buildComparator())
            .collect(Collectors.toList());

        // setAll() меняет содержимое существующего списка, не заменяя сам список —
        // поэтому TableView не трогает sortOrder.
        tableData.setAll(filtered);
        CanvasUtility.redrawCanvas(canvas, filtered);
    }

    /**
     * Возвращает строковое представление значения указанного столбца для фильтрации.
     */
    private String extractColumnValue(OrganizationWithId org, FilterColumn col) {
        if (col == null) return org.getData().getName();
        return switch (col) {
            case ID              -> String.valueOf(org.getId());
            case NAME            -> org.getData().getName();
            case X               -> String.valueOf(org.getData().getCoordinates().getX());
            case Y               -> String.valueOf(org.getData().getCoordinates().getY());
            case ANNUAL_TURNOVER -> String.valueOf(org.getData().getAnnualTurnover());
            case FULL_NAME       -> org.getData().getFullName();
            case ORG_TYPE        -> org.getData().getOrganizationType() != null
                                      ? org.getData().getOrganizationType().toString() : "";
            case ADDRESS         -> org.getData().getOfficialAddress() != null
                                      ? org.getData().getOfficialAddress().getStreet() : "";
            case ZIP_CODE        -> org.getData().getOfficialAddress() != null
                                      ? org.getData().getOfficialAddress().getZipCode() : "";
            case OWNER           -> org.getOwner();
        };
    }

    /**
     * Строит Comparator на основе TableView.sortOrder и TableColumn.sortType.
     * Используется внутри Stream.sorted().
     */
    @SuppressWarnings("unchecked")
    private Comparator<OrganizationWithId> buildComparator() {
        List<TableColumn<OrganizationWithId, ?>> sortOrder = tableView.getSortOrder();
        if (sortOrder.isEmpty()) {
            // По умолчанию — по ID
            return Comparator.comparingLong(OrganizationWithId::getId);
        }

        // Composable компаратор по всем столбцам из sortOrder
        Comparator<OrganizationWithId> comparator = null;
        for (TableColumn<OrganizationWithId, ?> tc : sortOrder) {
            Comparator<OrganizationWithId> colComparator = comparatorForColumn(tc);
            if (tc.getSortType() == TableColumn.SortType.DESCENDING) {
                colComparator = colComparator.reversed();
            }
            comparator = comparator == null ? colComparator : comparator.thenComparing(colComparator);
        }
        return comparator != null ? comparator : Comparator.comparingLong(OrganizationWithId::getId);
    }

    /** Возвращает Comparator, соответствующий конкретному столбцу. */
    private Comparator<OrganizationWithId> comparatorForColumn(TableColumn<OrganizationWithId, ?> tc) {
        if (tc == idColumn) {
            return Comparator.comparingLong(OrganizationWithId::getId);
        } else if (tc == nameColumn) {
            return Comparator.comparing(o -> o.getData().getName(),
                Comparator.nullsLast(String::compareToIgnoreCase));
        } else if (tc == xColumn) {
            return Comparator.comparingDouble(o -> o.getData().getCoordinates().getX());
        } else if (tc == yColumn) {
            return Comparator.comparingDouble(o -> (double) o.getData().getCoordinates().getY());
        } else if (tc == annualTurnoverColumn) {
            return Comparator.comparingDouble(o ->
                o.getData().getAnnualTurnover() != null ? o.getData().getAnnualTurnover() : 0.0);
        } else if (tc == fullNameColumn) {
            return Comparator.comparing(o -> o.getData().getFullName(),
                Comparator.nullsLast(String::compareToIgnoreCase));
        } else if (tc == orgTypeColumn) {
            return Comparator.comparing(o ->
                o.getData().getOrganizationType() != null
                    ? o.getData().getOrganizationType().toString() : "",
                Comparator.nullsLast(String::compareTo));
        } else if (tc == addressColumn) {
            return Comparator.comparing(o ->
                o.getData().getOfficialAddress() != null
                    ? o.getData().getOfficialAddress().getStreet() : "",
                Comparator.nullsLast(String::compareToIgnoreCase));
        } else if (tc == zipCodeColumn) {
            return Comparator.comparing(o ->
                o.getData().getOfficialAddress() != null
                    ? o.getData().getOfficialAddress().getZipCode() : "",
                Comparator.nullsLast(String::compareToIgnoreCase));
        } else if (tc == ownerColumn) {
            return Comparator.comparing(o -> o.getOwner(),
                Comparator.nullsLast(String::compareToIgnoreCase));
        }
        // Фолбэк — по ID
        return Comparator.comparingLong(OrganizationWithId::getId);
    }

    // ---------- Команды ----------

    @FXML public void add()   { sendReq("add"); }
    @FXML public void info()  { sendReq("info"); }
    @FXML public void clear() { sendReq("clear"); }
    @FXML public void help() { sendReq("help"); }
    @FXML public void history() { sendReq("history"); }

    @FXML
    public void send() {
        String input = console.getText().trim();
        if (input.isEmpty()) return;
        console.clear();
        sendReq(input);
    }

    public void sendReq(String input) {
        ValidationResult validation = commands.validate(input);
        if (validation.isError()) {
            String message = lm.get(String.valueOf(validation.error().get()).toLowerCase());
            out.appendText(lm.get("error.prefix") + message + "\n\n");
            return;
        }

        Request request = validation.request().get();

        if (request.getCommandType() == CommandType.HISTORY) {
            out.appendText(ClientCommands.printHistory(history, lm) + "\n\n");
            return;
        }
        if (request.getCommandType() == CommandType.HELP) {
            out.appendText(ClientCommands.printHelp(request.getPayload(), commands.getCommands(), lm) + "\n\n");
            return;
        }
        if (request.getCommandType() == CommandType.EXIT) return;

        if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
            String result = ClientCommands.executeScript(request, commands, lm);
            out.appendText(result + "\n\n");
            history.add(request);
            loadTable();
            return;
        }

        // Выполняем сетевой запрос в фоновом потоке — UI не блокируется.
        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                return App.sendRequest(request);
            }
        };

        task.setOnSucceeded(e -> {
            Response response = task.getValue();
            history.add(request);

            String key = response.getMessage();
            out.appendText(String.format(lm.get(key), response.getArgs().toArray()) + "\n\n");
            loadTable(); // тоже асинхронный — не блокирует

            if (request.getCommandType() == CommandType.ADD
                    || request.getCommandType() == CommandType.ADD_IF_MIN) {
                List<OrganizationWithId> items = tableView.getItems();
                if (!items.isEmpty()) {
                    CanvasUtility.animateOrg(canvas, items.get(items.size() - 1));
                }
            }
        });

        task.setOnFailed(e ->
            out.appendText(lm.get("error.prefix") + task.getException().getLocalizedMessage() + "\n\n")
        );

        new Thread(task, "send-request-thread").start();
    }

    // ---------- Поллинг ----------

    /**
     * Запускает фоновый опрос сервера раз в 3 секунды.
     * Повторные вызовы (после каждого loadTable) игнорируются —
     * Timeline создаётся ровно один раз за время жизни контроллера.
     */
    private void startPolling() {
        if (pollingTimeline != null) return; // уже запущен — не создавать ещё один

        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            // Сетевой запрос выполняется в фоновом потоке (см. sendReq),
            // но поллинг намеренно лёгкий и редкий, поэтому оставляем
            // обработку ответа через Platform.runLater.
            try {
                Request request = new Request(CommandType.SHOW);
                Response response = App.sendRequest(request);
                List<OrganizationWithId> newData = response.getData();
                Platform.runLater(() -> {
                    allData = new ArrayList<>(newData);
                    applyFilterAndSort();
                });
            } catch (Exception ex) {
                // тихо игнорируем ошибки поллинга
            }
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }
}
