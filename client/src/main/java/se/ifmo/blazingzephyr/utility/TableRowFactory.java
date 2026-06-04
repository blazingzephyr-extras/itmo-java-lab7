package se.ifmo.blazingzephyr.utility;

import java.util.function.Consumer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import se.ifmo.blazingzephyr.model.OrganizationWithId;

public class TableRowFactory {
    
    public static TableRow<OrganizationWithId> factory(
        TableView view,
        Consumer<TableRow<OrganizationWithId>> deleteAction,
        Consumer<TableRow<OrganizationWithId>> editAction
        ) {
            TableRow<OrganizationWithId> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem("Удалить");
            deleteItem.setOnAction(e -> deleteAction.accept(row));

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            MenuItem editItem = new MenuItem("Редактировать");
            editItem.setOnAction(e -> editAction.accept(row));
            contextMenu.getItems().addAll(editItem, deleteItem);

            return row;
    }
}
