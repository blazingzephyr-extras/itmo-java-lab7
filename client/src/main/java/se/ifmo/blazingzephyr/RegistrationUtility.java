package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

/**
 * Утилита для обработки запросов на регистрацию.
 * @author blazingzephyr
 * @version 1.0
 */
public class RegistrationUtility {
    
    public static boolean register(CommandType type, DatagramSocket socket, InetAddress address, int port, String login, String password) {

        Request request = new Request(type);
        request.packAuthorization(login, password);
        
        // Сериализуем запрос.
        byte[] buffer;
        try {
            buffer = Serializer.serialize(request);
        } catch (IOException e) {
            System.out.println("Ошибка сериализации запроса: " + e.getLocalizedMessage());
            return false;
        }
        
        // Отправляем запрос на регистрацию серверу.
        DatagramPacket requestDatagram = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.send(requestDatagram);
        } catch (IOException e) {
            System.out.println("Ошибка отправки пакета: " + e.getLocalizedMessage());
            return false;
        }
    
        // Получаем ответ сервера и выводим его.
        buffer = new byte[3000];
        DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(responseDatagram);
        } catch (IOException e) {
            System.out.println("Ошибка получения пакета: " + e.getLocalizedMessage());
            return false;
        }
    
        try {
            Response response = Serializer.deserialize(responseDatagram.getData());
            System.out.println(response.getMessage());
            return response.isSuccess();
        } catch (Exception e) {
            System.out.println("Ошибка декодирования пакета: " + e.getLocalizedMessage());
            return false;
        }
    }
}
