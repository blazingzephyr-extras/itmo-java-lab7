package se.ifmo.blazingzephyr.controllers;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.i18n.LocaleManager;

public class RootController {
    
    @FXML private TextField passwordTextField;
    @FXML private Button loginButton;

    // Поле для логина и пароля (метки)
    @FXML private Label passwordLabel;

    // Выбор языка
    private final LocaleManager lm = LocaleManager.getInstance();

    // Новый пароль.
    private boolean saved;
    private String newPassword;
    
    @FXML
    public void initialize() {
        applyLocale();
    }

    /** Обновляет все тексты в соответствии с текущей локалью. */
    private void applyLocale() {
        passwordLabel.setText(lm.get("change.password.must.change"));
    }

    @FXML
    private void save() {
        String text = this.passwordTextField.getText();
        if (text.isEmpty() || text.equals("root")) {
            App.showPopup(lm.get("change.password.incorrect"));
        }
        else {
            this.newPassword = text;
            this.saved = true;
            
            // Закрываем окно.
            Stage stage = (Stage)loginButton.getScene().getWindow();
            stage.close();
        }
    }

    public boolean isSaved() { return this.saved; }
    public String getPassword() { return this.newPassword; }
}
