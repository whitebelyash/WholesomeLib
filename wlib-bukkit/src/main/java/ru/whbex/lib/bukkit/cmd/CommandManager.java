package ru.whbex.lib.bukkit.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import ru.whbex.lib.reflect.ReflectUtils;

import java.util.List;

public class CommandManager {

    private static final String COMMAND_MAP_NAME = "commandMap";


    public static void registerCommand(String prefix,Command command){
        try {
            // Can break if bukkit/paper devs change this map name
            // I DO hate Bukkit
            CommandMap cmdMap = ReflectUtils.getDeclField(Bukkit.getServer(), COMMAND_MAP_NAME);
            cmdMap.register(prefix, command);
        // TODO: Properly handle exceptions here
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static void registerCommand(String prefix, List<Command> commands){
        try {
            CommandMap cmdMap = ReflectUtils.getDeclField(Bukkit.getServer(), COMMAND_MAP_NAME);
            cmdMap.registerAll(prefix, commands);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
