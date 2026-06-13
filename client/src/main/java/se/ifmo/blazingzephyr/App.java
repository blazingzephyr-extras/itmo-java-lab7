package se.ifmo.blazingzephyr;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Optional;

import se.ifmo.blazingzephyr.controllers.OrganizationDialogController;
import se.ifmo.blazingzephyr.controllers.PrimaryController;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Popup popup;

    // Application управляет логином и паролем,
    // чтобы не передавать их отдельно лишний раз в другие классы.
    private static DatagramSocket socket;
    private static String login;
    private static String password;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(App.class.getResource("auth.fxml"));
        Parent fxml = loader.load();

        scene = new Scene(fxml);
        stage.setScene(scene);
        stage.show();
    }

    public static void authorize(String lgin, String pwd) throws IOException {

        login = lgin;
        password = pwd;

        FXMLLoader loader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent fxml = loader.load();
        PrimaryController controller = loader.getController();

        controller.setLogin(login);
        controller.loadTable();
        scene.setRoot(fxml);
    }

    public static int getPort() {
        return 2100;
    }

    public static DatagramSocket getSocket() {
        if (socket == null) {
            try {
                socket = new DatagramSocket();
                socket.setSoTimeout(5000); // таймаут 5 секунд
            } catch (SocketException e) {
                System.out.println("Socket error: " + e.getLocalizedMessage());
            }
        }
        return socket;
    }

    public static void showPopup(String message) {
        if (popup == null) {
            popup = new Popup();

            Label label = new Label(message);
            label.setStyle(
                "-fx-background-color: #323232;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 20 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 14px;"
            );

            popup.getContent().add(label);
        } else {
            Label label = (Label) popup.getContent().get(0);
            label.setText(message);
        }

        popup.show(scene.getWindow());
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        // Автоскрытие через 3 секунды
        new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            popup.hide();
            scene.getWindow().requestFocus();
        })).play();
    }

    public static Response sendRequest(Request request) throws IOException, ClassNotFoundException {

        // Добавляем в запрос логин и пароль.
        request.packAuthorization(login, password);

        // Сериализуем команды.
        byte[] buffer = Serializer.serialize(request);

        // Отправляет команду серверу.
        DatagramPacket requestDatagram = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), App.getPort());
        
        // Отправка на сервер датаграммы.
        App.getSocket().send(requestDatagram);
    
        // Получает ответ сервера и выводит его.
        buffer = new byte[65507];
        
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);
        try {
            getSocket().receive(responseDatagram);
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Server did not respond within 5 seconds. Check the connection.", e);
        }
        
        Response response = Serializer.deserialize(responseDatagram.getData());
        return response;
    }

    public static Optional<OrganizationData> showOrganizationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("dialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));
            dialog.showAndWait();

            OrganizationDialogController controller = loader.getController();
            return Optional.ofNullable(controller.getResult());
        } catch (Exception e) {
            App.showPopup("Could not open the window.");
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}