package ru.whbex.lib.bukkit.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

/* CommandExecutor command implementation */
public class StubCommand extends Command {
    private final CommandExecutor executor;
    public StubCommand(String name, CommandExecutor executor) {
        super(name);
        this.executor = executor;
    }
    public StubCommand(String name, String description, String usageMessage, List<String> aliases, CommandExecutor executor) {
        super(name, description, usageMessage, aliases);
        this.executor = executor;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        return executor.onCommand(commandSender, this, s, strings);
    }
}
