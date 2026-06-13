package se.ifmo.blazingzephyr.controllers;

import java.io.IOException;
import java.net.InetAddress;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
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

        // Показываем название локали
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

        Pair<Boolean, String> res = RegistrationUtility.register(
            type,
            App.getSocket(),
            InetAddress.getLocalHost(),
            App.getPort(),
            login, password,
            lm
        );

        if (res.getValue().equals("register.auth_root_initial")) {
            String assignedPassword = showChangeRootPasswordDialog(lm);
            if (assignedPassword == null) return;

            Pair<Boolean, String> passwordChanged = RegistrationUtility.register(
                CommandType.UPDATE_ROOT_PASSWORD,
                App.getSocket(),
                InetAddress.getLocalHost(),
                App.getPort(),
                login,
                assignedPassword,
                lm
            );

            App.showPopup(lm.get(passwordChanged.getValue()));

            if (!passwordChanged.getKey()) return;
            else password = assignedPassword;
        }

        if (res.getKey()) {
            App.authorize(login, password);
        } else {
            String message = register ? "auth.error.register" : "auth.error.login";
            App.showPopup(lm.get(message));
        }
    }

    private String showChangeRootPasswordDialog(LocaleManager lm) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("root_pwd.fxml"));
            Parent root = loader.load();
            
            RootController controller = loader.getController();            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                return controller.getPassword();
            }
            else {
                App.showPopup(lm.get("change.password.cancel"));
                return null;
            }
        } catch (IOException e) {
            App.showPopup(lm.get("change.password.error") + ": " + e.getMessage());
            return null;
        }
    }
}