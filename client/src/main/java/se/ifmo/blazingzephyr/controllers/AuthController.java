package se.ifmo.blazingzephyr.controllers;

import java.io.IOException;
import java.net.InetAddress;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import se.ifmo.blazingzephyr.App;
import se.ifmo.blazingzephyr.RegistrationUtility;
import se.ifmo.blazingzephyr.networking.CommandType;

public class AuthController {

    @FXML
    private TextField loginTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private CheckBox registerNewCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private void flipMode() throws IOException {
        if (registerNewCheckBox.isSelected()) {
            loginButton.setText("Регистрация");
        } else {
            loginButton.setText("Войти");
        }
    }

    @FXML
    private void login() throws IOException {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();
        
        boolean register = registerNewCheckBox.isSelected();
        CommandType type = register ? CommandType.REGISTER : CommandType.AUTHORIZE;

        if (RegistrationUtility.register(type, App.getSocket(), InetAddress.getLocalHost(), App.getPort(), login, password)) {

            App.authorize(login, password);
        }
        else {
            App.showPopup("Не удалось " + (register ? "зарегистрироваться." : "войти."));
        }
    }
}
