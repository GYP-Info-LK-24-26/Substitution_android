package de.igelstudios.substitution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Util {
    public static class TriInt implements Comparable<TriInt>{
        public int first;
        public int second;
        public int third;

        @Override
        public int compareTo(TriInt o) {
            int a = Integer.compare(first,o.first);
            if(a != 0)return -a;
            int b = Integer.compare(second,o.second);
            if(b != 0)return -b;
            return -Integer.compare(third,o.third);
        }
    }

    public static boolean isCanceled(Substitution substitution){
        return substitution.info.contains("Studierzeit") || substitution.info.contains("entfällt");
    }
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

    public static TriInt versionFromString(String version){
        TriInt triInt = new TriInt();
        int firstSplit = version.indexOf('.');
        triInt.first = Integer.parseInt(version.substring(0,firstSplit));
        int secondSplit = version.indexOf(firstSplit + 1,'.');
        if(secondSplit == -1){
            triInt.second = Integer.parseInt(version.substring(firstSplit + 1));
            return triInt;
        }
        triInt.second = Integer.parseInt(version.substring(firstSplit + 1,secondSplit));
        int thirdSplit = secondSplit + 1;
        while (thirdSplit < version.length()){
            if(!Character.isDigit(version.charAt(thirdSplit)))break;
            thirdSplit++;
        }

        triInt.third = Integer.parseInt(version.substring(secondSplit + 1,thirdSplit));
        return triInt;
    }

    public static String timeToDate(LocalDate date){
        String day = String.format(Locale.getDefault(),"%02d",date.getDayOfMonth());
        String month = String.format(Locale.getDefault(),"%02d",date.getMonthValue());
        String year = String.valueOf(date.getYear());
        return day + "." + month + "." + year;
    }
}
