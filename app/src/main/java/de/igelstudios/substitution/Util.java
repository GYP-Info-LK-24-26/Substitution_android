package de.igelstudios.substitution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Util {
    public static byte[] read(final InputStream input) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        if ((bytesRead = input.read(buffer)) != -1) stream.write(buffer, 0, bytesRead);
        return stream.toByteArray();
    }

    public static LocalDate dateFromString(String string){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
        return LocalDate.parse(string, formatter);
    }
}
