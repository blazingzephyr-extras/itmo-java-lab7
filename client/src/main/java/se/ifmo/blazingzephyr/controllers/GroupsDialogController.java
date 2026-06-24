package se.ifmo.blazingzephyr.controllers;

import java.sql.Date;
import java.util.List;
import java.util.MissingResourceException;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.i18n.LocaleManager;

import se.ifmo.blazingzephyr.model.User;
import se.ifmo.blazingzephyr.model.Group;
import se.ifmo.blazingzephyr.model.UserGroupRelation;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;

public class GroupsDialogController {

    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML private TableView<User> users;
    @FXML private TableColumn<User, Long> users_idColumn;
    @FXML private TableColumn<User, String> users_loginColumn;
    @FXML private TableColumn<User, String> users_passwordColumn;

    @FXML private TableView<Group> groups;
    @FXML private TableColumn<Group, Long> groups_idColumn;
    @FXML private TableColumn<Group, String> groups_nameColumn;
    @FXML private TableColumn<Group, Date> groups_createdAt;

    @FXML private TableView<UserGroupRelation> relations;
    @FXML private TableColumn<UserGroupRelation, Long> relations_userId;
    @FXML private TableColumn<UserGroupRelation, Long> relations_groupId;
    @FXML private TableColumn<UserGroupRelation, Date> relations_addedAt;

    @FXML private Button create_group;
    @FXML private Button add_user;
    @FXML private TextArea out;

    /**
     * ObservableList, который навсегда привязан к TableView.
     * Мы никогда не делаем setItems() повторно — только меняем содержимое этого списка.
     * Это ключевое: TableView сбрасывает sortOrder именно при замене самого списка.
     */
    private final javafx.collections.ObservableList<User> userData =
        FXCollections.observableArrayList();

    private final javafx.collections.ObservableList<Group> groupData =
        FXCollections.observableArrayList();

    private final javafx.collections.ObservableList<UserGroupRelation> relationData =
        FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        users_idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        users_loginColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().login()));
        users_passwordColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().password()));

        groups_idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        groups_nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        groups_createdAt.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().createdAt()));

        relations_userId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().userId()));
        relations_groupId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().groupId()));
        relations_addedAt.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().addedAt()));

        users.setItems(userData);
        groups.setItems(groupData);
        relations.setItems(relationData);

        // ПКМ по строке группы -- удалить группу.
        groups.setRowFactory(tv -> buildGroupRow());

        // ПКМ по строке отношения "пользователь-группа" -- удалить пользователя из группы.
        relations.setRowFactory(tv -> buildRelationRow());

        // Подписываемся на смену локали
        lm.bundleProperty().addListener((obs, o, n) -> applyLocale());
        applyLocale();
    }

    /** Обновляет все тексты полей согласно текущей локали. */
    private void applyLocale() {
        // Обновить заголовки столбцов
        users_idColumn.setText(lm.get("col.users.id"));
        users_loginColumn.setText(lm.get("col.users.login"));
        users_passwordColumn.setText(lm.get("col.users.password"));
        groups_idColumn.setText(lm.get("col.groups.id"));
        groups_nameColumn.setText(lm.get("col.groups.name"));
        groups_createdAt.setText(lm.get("col.groups.createdAt"));
        relations_userId.setText(lm.get("col.relations.userId"));
        relations_groupId.setText(lm.get("col.relations.groupId"));
        relations_addedAt.setText(lm.get("col.relations.addedAt"));
        create_group.setText(lm.get("creategroup"));
        add_user.setText(lm.get("adduser"));
    }

    /**
     * Безопасно получает строку из бандла: если ключ отсутствует,
     * возвращает сам ключ вместо MissingResourceException.
     */
    private String str(String key) {
        try {
            return lm.get(key);
        } catch (java.util.MissingResourceException e) {
            return key;
        }
    }

    /**
     * Загружает все три таблицы с сервера в фоновом потоке.
     * Возвращает CompletableFuture, который можно дождаться перед мутацией.
     * Теперь не используется автоматически при каждом действии – вместо этого
     * вызывается один раз при открытии окна (через loadInitialData()).
     */
    public java.util.concurrent.CompletableFuture<Void> loadTable() {
        return java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Request usersReq     = new Request(CommandType.GET_USERS);
                Request groupsReq    = new Request(CommandType.GET_GROUPS);
                Request relationsReq = new Request(CommandType.GET_RELATIONS);

                var usersResp     = App.sendRequest(usersReq);
                var groupsResp    = App.sendRequest(groupsReq);
                var relationsResp = App.sendRequest(relationsReq);

                List<User> newUsers = usersResp.getArgs().stream()
                        .filter(a -> a instanceof User)
                        .map(a -> (User) a)
                        .toList();

                List<Group> newGroups = groupsResp.getArgs().stream()
                        .filter(a -> a instanceof Group)
                        .map(a -> (Group) a)
                        .toList();

                List<UserGroupRelation> newRelations = relationsResp.getArgs().stream()
                        .filter(a -> a instanceof UserGroupRelation)
                        .map(a -> (UserGroupRelation) a)
                        .toList();

                javafx.application.Platform.runLater(() -> {
                    userData.setAll(newUsers);
                    groupData.setAll(newGroups);
                    relationData.setAll(newRelations);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        out.appendText("[loadTable error] " + e.getMessage() + "\n"));
            }
        });
    }

    /**
     * Метод для вызова сразу после отображения диалога.
     * Загружает актуальные данные таблиц с сервера.
     */
    public void loadInitialData() {
        loadTable();
    }

    @FXML
    public void createGroup() {
        // Больше не вызываем loadTable() перед показом диалога.
        // Данные уже загружены при открытии окна (loadInitialData()).
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle(str("creategroup"));
            dialog.setHeaderText(null);
            dialog.setContentText(str("col.groups.name") + ":");

            dialog.showAndWait().ifPresent(name -> {
                if (name.isBlank()) return;

                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        Request req = new Request(
                                CommandType.CREATE_GROUP,
                                new se.ifmo.blazingzephyr.networking.CommandPayload.StringArg(name.trim()));
                        var resp = App.sendRequest(req);

                        javafx.application.Platform.runLater(() ->
                                out.appendText(str(resp.isSuccess()
                                        ? "groups.created"
                                        : resp.getMessage()) + "\n"));

                        if (resp.isSuccess()) loadTable(); // обновить таблицы после успешного создания
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() ->
                                out.appendText("[createGroup error] " + e.getMessage() + "\n"));
                    }
                });
            });
        });
    }

    /**
     * Строит строку таблицы групп с контекстным меню (ПКМ) для удаления группы.
     */
    private TableRow<Group> buildGroupRow() {
        TableRow<Group> row = new TableRow<>();

        MenuItem deleteItem = new MenuItem();
        deleteItem.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> str("groups.menu.delete"), lm.bundleProperty()));
        deleteItem.setOnAction(e -> {
            Group group = row.getItem();
            if (group != null) deleteGroup(group);
        });

        ContextMenu menu = new ContextMenu(deleteItem);

        row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(menu));

        return row;
    }

    /**
     * Строит строку таблицы отношений «пользователь-группа» с контекстным меню (ПКМ)
     * для удаления пользователя из группы.
     */
    private TableRow<UserGroupRelation> buildRelationRow() {
        TableRow<UserGroupRelation> row = new TableRow<>();

        MenuItem removeItem = new MenuItem();
        removeItem.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> str("groups.menu.removeFromGroup"), lm.bundleProperty()));
        removeItem.setOnAction(e -> {
            UserGroupRelation relation = row.getItem();
            if (relation != null) removeUserFromGroup(relation);
        });

        ContextMenu menu = new ContextMenu(removeItem);

        row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(menu));

        return row;
    }

    /** Удаляет группу по ПКМ-меню и обновляет таблицы при успехе. */
    private void deleteGroup(Group group) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Request req = new Request(
                        CommandType.DELETE_GROUP,
                        new se.ifmo.blazingzephyr.networking.CommandPayload.LongArg(group.id()));
                var resp = App.sendRequest(req);

                javafx.application.Platform.runLater(() ->
                        out.appendText(str(resp.isSuccess()
                                ? "groups.deleted"
                                : resp.getMessage()) + "\n"));

                if (resp.isSuccess()) loadTable();
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        out.appendText("[deleteGroup error] " + e.getMessage() + "\n"));
            }
        });
    }

    /** Удаляет пользователя из группы по ПКМ-меню и обновляет таблицы при успехе. */
    private void removeUserFromGroup(UserGroupRelation relation) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Request req = new Request(
                        CommandType.REMOVE_USER_FROM_GROUP,
                        new se.ifmo.blazingzephyr.networking.CommandPayload.TwoLongs(
                                relation.userId(), relation.groupId()));
                var resp = App.sendRequest(req);

                javafx.application.Platform.runLater(() ->
                        out.appendText(str(resp.isSuccess()
                                ? "groups.user_removed"
                                : resp.getMessage()) + "\n"));

                if (resp.isSuccess()) loadTable();
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        out.appendText("[removeUserFromGroup error] " + e.getMessage() + "\n"));
            }
        });
    }

    @FXML
    public void addUser() {
        // Данные уже актуальны благодаря loadInitialData().
        // Проверяем выделение немедленно, не дожидаясь загрузки.
        javafx.application.Platform.runLater(() -> {
            User selectedUser   = users.getSelectionModel().getSelectedItem();
            Group selectedGroup = groups.getSelectionModel().getSelectedItem();

            if (selectedUser == null || selectedGroup == null) {
                out.appendText(str("groups.select_both") + "\n");
                return;
            }

            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Request req = new Request(
                            CommandType.ADD_USER_TO_GROUP,
                            new se.ifmo.blazingzephyr.networking.CommandPayload.TwoLongs(
                                    selectedUser.id(), selectedGroup.id()));
                    var resp = App.sendRequest(req);

                    javafx.application.Platform.runLater(() ->
                            out.appendText(str(resp.isSuccess()
                                    ? "groups.user_added"
                                    : resp.getMessage()) + "\n"));

                    if (resp.isSuccess()) loadTable(); // обновить таблицы после успешного добавления
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                            out.appendText("[addUser error] " + e.getMessage() + "\n"));
                }
            });
        });
    }
}
