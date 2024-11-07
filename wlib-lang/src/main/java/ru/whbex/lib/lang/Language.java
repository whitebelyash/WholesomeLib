package ru.whbex.lib.lang;

import org.slf4j.event.Level;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.log.Debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Language {
    private final LanguageFile file;

    private String name;
    private String nameLocalized;
    private Locale locale;

    private final Map<String, String> phrases = new HashMap<>();

    public Language(LanguageFile file){
        try {
            file.open();
        } catch (IOException e) {
            LogContext.log(Level.ERROR, "Failed to initialize language file at " + file.getFile().getPath());
            e.printStackTrace();
        }
        this.file = file;
        // LangFile is now pointing at first line
        try {
            while(file.hasNextLine()){
                if(file.isCommented()){
                    file.next(); continue;
                }
                String[] phrase = file.getCurrentPhrase();
                phrases.put(phrase[0], phrase[1]);
                file.next();
            }
        } catch (Exception e) {
            LogContext.log(Level.ERROR, "Failed reading LangFile {0} at {1} line", file.getFile().getName(), file.getPosition());
        }
        Debug.print("Loaded Language at {0}. Will load metadata now", file.toString());
        this.loadMetadata();
        try {
            file.close();
        } catch (IOException e) {
            Debug.print("Failed to close LangFile {0}!", file.toString());
        }
    }

    private void loadMetadata(){
        if((name = phrases.get("locale")) == null)
            LogContext.log(Level.WARN, "Locale {0} has no name!", file.getFile().getName());
        if((locale = Locale.forLanguageTag(phrases.getOrDefault("locale.tag", ""))) == null)
            LogContext.log(Level.WARN, "Locale {0} has no locale tag, or it's invalid!", file.getFile().getName());
        if((nameLocalized = phrases.get("locale.name")) == null)
            LogContext.log(Level.WARN, "Locale {0} has no localized name! Check locale.name tag in the file.", file.getFile().getName());
    }

    public String getName() {
        return name;
    }
    public String getNameLocalized() {
        return nameLocalized;
    }

    public Locale getLocale() {
        return locale;
    }
    public boolean hasPhrase(String key){
        return phrases.containsKey(key);
    }
    public String getPhrase(String key){
        return hasPhrase(key) ? phrases.get(key) : key;
    }


    public Map<String, String> getPhrases() {
        return phrases;
    }

    @Override
    public String toString() {
        return "Language{" +
                "locale=" + locale +
                ", nameLocalized='" + nameLocalized + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
