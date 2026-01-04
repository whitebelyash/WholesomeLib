package ru.whbex.lib.bukkit.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import ru.whbex.lib.reflect.ReflectUtils;

import java.lang.reflect.Field;
import java.util.List;

public class CommandManager {

    private static final CommandMap commandMap;
    public static final String FALLBACK_PREFIX = "changeme";

    static {
        try {
            // Can break if bukkit/paper devs change this map name
            // I DO hate Bukkit
            commandMap = ReflectUtils.getDeclField(Bukkit.getServer(), "commandMap");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerCommand(String prefix,Command command){
        commandMap.register(prefix, command);
    }
    public static void registerCommand(String prefix, String name, CommandExecutor executor){
        StubCommand sc = new StubCommand(name, executor);
        commandMap.register(prefix, sc);
    }
    public static void registerCommand(String prefix, List<Command> commands){
        commandMap.registerAll(prefix, commands);
    }
    public static void dispatchCommand(CommandSender sender, String command) {
        commandMap.dispatch(sender, command);
    }

    public static class ChainedRegister {
        private final String prefix;

        public ChainedRegister(String prefix){
            this.prefix = prefix;
        }
        public ChainedRegister register(StubCommand command){
            CommandManager.registerCommand(prefix, command);
            return this;
        }
        public ChainedRegister register(String name, CommandExecutor executor){
            CommandManager.registerCommand(prefix, name, executor);
            return this;
        }
        // More will come
    }
}
