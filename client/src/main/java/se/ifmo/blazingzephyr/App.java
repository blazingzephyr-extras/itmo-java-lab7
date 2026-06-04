package se.ifmo.blazingzephyr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    private static String login;
    private static String password;
    private static Scene scene;
    private static Popup popup;

    private static DatagramSocket socket;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("auth"), 640, 480);
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
            }

            // Обработки ошибки сокета.
            catch (SocketException e) {
                System.out.println("Ошибка сокета: " + e.getLocalizedMessage());
            }
        }
        return socket;
    }

    public static void showPopup(String message) {
        
        if (popup == null) {
            popup = new Popup();
            popup.getContent().add(new Label(message));
        }
        else {
            Label label = (Label)popup.getContent().get(0);
            label.setText(message);
        }

        popup.show(scene.getWindow());
        popup.setAutoHide(true); // Автоматическое скрытие
        popup.setHideOnEscape(true); // Закрытие при нажатии Esc
    }

    public static Response sendRequest(Request request) throws IOException, ClassNotFoundException {

        // Добавляем в запрос логин и пароль.
        request.packAuthorization(login, password);
        
        // Сериализуем команды.
        byte[] buffer = Serializer.serialize(request);

        // Отправляет команду серверу.
        DatagramPacket requestDatagram = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), App.getPort());
        
        App.getSocket().send(requestDatagram);
    
        // Получает ответ сервера и выводит его.
        buffer = new byte[3000];
        
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);
        App.getSocket().receive(responseDatagram);
        
        Response response = Serializer.deserialize(responseDatagram.getData());
        return response;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
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
            App.showPopup("Не удалось открыть окно");
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}