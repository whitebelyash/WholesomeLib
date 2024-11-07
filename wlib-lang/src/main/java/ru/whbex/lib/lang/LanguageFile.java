package ru.whbex.lib.lang;


import ru.whbex.lib.log.LogDebug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Language file - java.io.File wrapper for .lang files
 * Format: key.key1.key2=value, key1.key2=   value
 * Everything after = belongs to value, quotes are not supported yet
 * TODO: Multiple lines
 * TODO: Quotes
 * TODO: spaces between separator
 * TODO: Unit tests?
 */
public final class LanguageFile {


    private final File file;
    private BufferedReader reader;
    private String current;
    private boolean open = false;
    private int pos = -1;
    private boolean empty = false;
    public LanguageFile(File file) {
        LogDebug.print("Creating language file at {0}", file.getAbsolutePath());
        this.empty = !file.exists();
        this.file = file;
    }

    /**
     * Open language file
     * @throws IOException on IO errors
     * @throws UnsupportedOperationException if file doesn't exist
     */
    public void open() throws IOException {
        if(empty)
            throw new UnsupportedOperationException("Language file does not exists, cannot open");
        reader = new BufferedReader(new FileReader(file));
        this.open = true;
        current = reader.readLine();
        pos = 1;
    }

    /**
     * Close language file. Does nothing if file is already closed.
     * @throws IOException on IO errors
     */
    public void close() throws IOException {
        if(!open)
            return;
        reader.close();
        open = false;
    }

    /**
     * Set language file reader position
     * @param pos position
     * @throws IOException on IO errors
     * @throws UnsupportedOperationException if language file is closed
     * @throws IllegalArgumentException if position is less than 1
     */
    public void setPosition(int pos) throws IOException {
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        if(pos < 1)
            throw new IllegalArgumentException("Invalid position!");
        this.pos = pos;
        reader.reset();
        for(int i = 0; i < pos - 1; i++){
            reader.readLine();
        }
        current = reader.readLine();
    }

    /**
     * Get language file reader position
     * @return position
     * @throws UnsupportedOperationException if language file is closed
     */

    public int getPosition() {
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        return pos;
    }

    /**
     * Check if next line exists in language file
     * @return check result
     * @throws IOException on IO errors
     * @throws UnsupportedOperationException if language file is closed
     */
    public boolean hasNextLine() throws IOException {
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        return open && current != null;
    }

    /**
     * Get full string (line) at current reader position
     * @return unformatted line (as is)
     * @throws UnsupportedOperationException if language file is closed
     */
    public String getCurrentString(){
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        return current;
    }

    /**
     * Is current string commented
     * @return check result
     * @throws UnsupportedOperationException if language file is closed
     */
    public boolean isCommented(){
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        return current.charAt(0) == '#';
    }

    /**
     * Returns phrase - 2-string array, where s[0] - key, [1] - value
     * @return array of length 2
     * @throws UnsupportedOperationException if language file is closed
     */
    public String[] getCurrentPhrase(){
        if(isCommented())
            return null;
        current = current.trim();
        String[] ret = current.split("=", 2);
        // value can be empty
        if(ret.length != 2 || ret[0].isEmpty())
            throw new IllegalArgumentException("Invalid string length!");
        // ignore commented text in locale value (everything after #)
        int commentIndex = ret[1].indexOf('#');
        if(commentIndex > 0 && ret[1].charAt(commentIndex - 1) != '\\'){
            ret[1] = ret[1].substring(0, commentIndex - 1);
        }
        return ret;
    }

    /**
     * Go to next line in language file
     * @throws IOException on IO errors
     * @throws UnsupportedOperationException if language file is closed
     * TODO: return false if reached file end
     */
    public void next() throws IOException {
        if(!open)
            throw new UnsupportedOperationException("Open language file before using it!");
        // may be broken
        do {
            pos++;
        } while ((current = reader.readLine()) != null && current.isEmpty());
    }

    /**
     * Is language file closed
     * @return check result
     */
    public boolean isClosed(){
        return open;
    }

    /**
     * Is language file exists
     * @return check result
     */
    public boolean exists(){
        return empty;
    }

    /**
     * Get IO File that this language file wraps
     * @return
     */
    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "LangFile{" +
                "file=" + file +
                ", current='" + current + '\'' +
                ", open=" + open +
                ", pos=" + pos +
                ", empty=" + empty +
                '}';
    }
}
