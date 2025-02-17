package de.igelstudios.substitution;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static Logger instance;
    private File logFile;
    private Context context;
    private static final String tag = "Substitution";

    public Logger(Context context){
        instance = this;
        this.context = context;
        createFile();
    }

    public void createFile(){
        logFile = new File(context.getCacheDir(),"log.txt");
        if(!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(Throwable throwable){
        if(!Config.get().shouldLog())return;
        try (PrintWriter writer = new PrintWriter(logFile)) {
            writer.append(tag).append(": ");
            throwable.printStackTrace(writer);
            writer.append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String string){
        if(!Config.get().shouldLog())return;
        try (PrintWriter writer = new PrintWriter(logFile)) {
            writer.append(tag).append(": ").append(string).append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger get(){
        return instance;
    }

    public void toClipBoard(){
        try(FileInputStream fis = new FileInputStream(logFile)) {
            byte[] data = new byte[fis.available()];
            if(fis.read(data) == -1)return;
            ClipboardManager clipboard = getSystemService(context,ClipboardManager.class);
            ClipData clip = ClipData.newPlainText("Substitution error log", new String(data));
            clipboard.setPrimaryClip(clip);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
