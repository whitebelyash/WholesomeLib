package ru.whbex.lib.lang;

import org.slf4j.event.Level;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.log.Debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Language {
    private final static char NEW_LINE_CHAR = '\n';
    private final LanguageFile file;

    private String name;
    private String nameLocalized;
    private Locale locale;

    private final Map<String, String> phrases = new HashMap<>();

    public Language(LanguageFile file) {
        this.file = file;
    }

    public void load() throws IOException {
        file.open();
        this.loadPhrases();
        this.loadMetadata();
        file.close();
    }

    private void loadMetadata(){
        if((name = phrases.get("locale")) == null)
            LogContext.log(Level.WARN, "Locale {0} has no name!", file.getFile().getName());
        if((locale = Locale.forLanguageTag(phrases.getOrDefault("locale.tag", ""))) == null)
            LogContext.log(Level.WARN, "Locale {0} has no locale tag, or it's invalid!", file.getFile().getName());
        if((nameLocalized = phrases.get("locale.name")) == null)
            LogContext.log(Level.WARN, "Locale {0} has no localized name! Check locale.name tag in the file.", file.getFile().getName());
    }
    private void loadPhrases(){
        try {
            while(file.hasNextLine()){
                if(file.isCommented()){
                    file.next(); continue;
                }
                String[] phrase = file.getCurrentPhrase();
                if(phrase == null){
                    file.next(); continue;
                }
                // Append new line to old phrase if key conflict occurs
                if(phrases.containsKey(phrase[0]))
                    phrases.put(phrase[0], phrases.get(phrase[0]) + NEW_LINE_CHAR + phrase[1]);
                else
                    phrases.put(phrase[0], phrase[1]);
                file.next();
            }
        } catch (Exception e) {
            LogContext.log(Level.ERROR, "Failed reading LangFile {0} at {1} line", file.getFile().getName(), file.getPosition());
        }
        Debug.print("Loaded {0} phrases from language file {1}", phrases.size(), file.toString());
    }

    public void reloadPhrases() throws IOException {
        file.open();
        file.setPosition(0); // reset position just in case
        phrases.clear();
        loadPhrases();
        loadMetadata();
        file.close();
    }
    public String getName() {
        return name == null ? file.getFile().getName() : name;
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
