package se.ifmo.blazingzephyr.utility;

import java.util.function.Consumer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import se.ifmo.blazingzephyr.i18n.LocaleManager;
import se.ifmo.blazingzephyr.model.OrganizationWithId;

public class TableRowFactory {

    public static TableRow<OrganizationWithId> factory(
        TableView<OrganizationWithId> view,
        Consumer<TableRow<OrganizationWithId>> deleteAction,
        Consumer<TableRow<OrganizationWithId>> editAction
    ) {
        LocaleManager lm = LocaleManager.getInstance();

        TableRow<OrganizationWithId> row = new TableRow<>();
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem(lm.get("menu.edit"));
        MenuItem deleteItem = new MenuItem(lm.get("menu.delete"));

        editItem.setOnAction(e -> editAction.accept(row));
        deleteItem.setOnAction(e -> deleteAction.accept(row));

        contextMenu.getItems().addAll(editItem, deleteItem);

        // Обновляем тексты при смене локали
        lm.bundleProperty().addListener((obs, o, n) -> {
            editItem.setText(lm.get("menu.edit"));
            deleteItem.setText(lm.get("menu.delete"));
        });

        row.contextMenuProperty().bind(
            javafx.beans.binding.Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
        );

        return row;
    }
    
}