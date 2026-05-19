package se.ifmo.blazingzephyr.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Утилита, читающая из файла.
 * 
 * ReadDatabase и WriteDatabase удалены по причине ненадобности.
 * 
 * @author blazingzephyr
 * @version 2.0
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
}
