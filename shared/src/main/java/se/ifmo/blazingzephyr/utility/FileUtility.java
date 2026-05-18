package se.ifmo.blazingzephyr.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Stack;
import se.ifmo.blazingzephyr.model.Organization;

/**
 * Утилита, читающая из файла.
 * @author blazingzephyr
 * @version 1.0
 */
public class FileUtility {
    
    /**
     * Читает файл и возвращает содержимое.
     * @param fpath Путь к файлу
     * @return список, содержащий все прочитанные строки
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedReader ReadBuffer(String fpath) throws FileNotFoundException, IOException
    {
        FileInputStream inputStream = new FileInputStream(fpath);
        InputStreamReader reader = new InputStreamReader(inputStream);
        return new BufferedReader(reader);
    }

    /**
     * Читает из CSV-БД и возвращает объекты.
     * @param fpath Путь к базе данных.
     * @return Пропарсенные объекты базы данных.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Stack<Organization> ReadDatabase(String fpath) throws FileNotFoundException, IOException
    {
        Stack<Organization> collection = new Stack<>();
        try (BufferedReader bufferReader = ReadBuffer(fpath)) {
            String line;
            while ((line = bufferReader.readLine()) != null) {
                Organization org = Organization.fromCsv(line);
                collection.add(org);
            }
        }

        return collection;
    }

    /**
     * Записывает коллекцию в файл.
     * @param fpath Путь к файлу, в который требуется записать коллекцюи.
     * @param collection Коллекция объектов, которые записываются в базу данных.
     * @throws IOException
     */
    public static void WriteDatabase(String fpath, Stack<Organization> collection) throws IOException
    {
        try (FileOutputStream outputStream = new FileOutputStream(fpath); 
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            BufferedWriter bufferWriter = new BufferedWriter(writer))
        {
            int s = collection.size();
            if (s > 0) {

                for (int i = 0; i < s - 1; i++) {
                    bufferWriter.write(collection.get(i).toCsv());
                    bufferWriter.write("\n");
                }
    
                bufferWriter.write(collection.get(s - 1).toCsv());
                bufferWriter.close();
            }
        }
    }
}
