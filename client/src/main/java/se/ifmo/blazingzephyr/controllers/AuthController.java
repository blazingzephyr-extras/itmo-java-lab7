package se.ifmo.blazingzephyr.controllers;

import java.io.IOException;
import java.net.InetAddress;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.RegistrationUtility;
import se.ifmo.blazingzephyr.i18n.LocaleManager;
import se.ifmo.blazingzephyr.networking.CommandType;

import java.util.Locale;

public class AuthController {

    @FXML private TextField loginTextField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox registerNewCheckBox;
    @FXML private Button loginButton;

    // Поле для логина и пароля (метки)
    @FXML private Label loginLabel;
    @FXML private Label passwordLabel;

    // Выбор языка
    @FXML private ChoiceBox<Locale> languageChoiceBox;

    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML
    public void initialize() {
        // Заполняем список языков
        languageChoiceBox.getItems().addAll(LocaleManager.SUPPORTED);

        // Показываем человекочитаемое название локали
        languageChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Locale locale) {
                return locale == null ? "" : lm.getDisplayName(locale);
            }
            @Override
            public Locale fromString(String s) { return null; }
        });

        languageChoiceBox.setValue(lm.getLocale());

        // При смене языка — переключаем без перезапуска
        languageChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lm.setLocale(newVal);
                applyLocale();
            }
        });

        // Подвязываем UI к текущей локали
        lm.bundleProperty().addListener((obs, o, n) -> applyLocale());
        applyLocale();
    }

    /** Обновляет все тексты в соответствии с текущей локалью. */
    private void applyLocale() {
        loginLabel.setText(lm.get("auth.loginField"));
        passwordLabel.setText(lm.get("auth.passwordField"));
        registerNewCheckBox.setText(lm.get("auth.registerCheckbox"));
        flipMode(); // Обновить надпись на кнопке
    }

    @FXML
    public void flipMode() {
        if (registerNewCheckBox.isSelected()) {
            loginButton.setText(lm.get("auth.register"));
        } else {
            loginButton.setText(lm.get("auth.login"));
        }
    }

    @FXML
    public void login() throws IOException {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        boolean register = registerNewCheckBox.isSelected();
        CommandType type = register ? CommandType.REGISTER : CommandType.AUTHORIZE;

        if (RegistrationUtility.register(type, App.getSocket(), InetAddress.getLocalHost(), App.getPort(), login, password)) {
            App.authorize(login, password);
        } else {
            String key = register ? "auth.error.register" : "auth.error.login";
            App.showPopup(lm.get(key));
        }
    }
}