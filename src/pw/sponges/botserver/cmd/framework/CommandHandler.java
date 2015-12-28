package pw.sponges.botserver.cmd.framework;

import pw.sponges.botserver.Bot;
import pw.sponges.botserver.cmd.commands.admin.*;
import pw.sponges.botserver.cmd.commands.fun.*;
import pw.sponges.botserver.cmd.commands.info.*;
import pw.sponges.botserver.cmd.commands.mc.*;
import pw.sponges.botserver.cmd.commands.op.*;
import pw.sponges.botserver.cmd.commands.steam.SteamStatusCommand;
import pw.sponges.botserver.cmd.commands.util.BridgeCommand;
import pw.sponges.botserver.cmd.commands.util.JSONBeautifier;
import pw.sponges.botserver.cmd.commands.util.JavaCommand;
import pw.sponges.botserver.framework.Room;
import pw.sponges.botserver.permissions.Group;
import pw.sponges.botserver.permissions.PermissionsManager;
import pw.sponges.botserver.permissions.simple.UserRole;
import pw.sponges.botserver.storage.Database;
import pw.sponges.botserver.storage.RoomSettings;
import pw.sponges.botserver.storage.Setting;
import pw.sponges.botserver.util.Msg;
import pw.sponges.botserver.util.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandHandler {

    private Bot bot;
    private List<Command> commands;
    private static Database database;
    private PermissionsManager permissions;

    public CommandHandler(Bot bot, PermissionsManager permissions) {
        this.bot = bot;
        this.permissions = permissions;
        this.commands = new ArrayList<>();
        database = bot.getDatabase();

        // TODO dynamic command registration
        this.registerCommands(
                new TestCommand(),
                new ChatInfoCommand(),
                new PrefixCommand(database),
                new BridgeCommand(bot),
                new AboutCommand(),
                new ClientsCommand(bot),
                new StopCommand(bot),
                new UrbanCommand(),
                new SettingsCommand(bot.getDatabase()),
                new ServerCommand(),
                new HelpCommand(database, this),
                new UserInfoCommand(),
                new MCNetworks(),
                new JoinRoomCommand(),
                new StatsCommand(),
                new GroupsCommand(database, this.permissions),
                new JSONBeautifier(),
                new CmdListCommand(this),
                new JavaCommand(bot),
                new TumblrArgumentCommand(),
                new GoogleCommand(),
                new LmgtfyCommand(),
                new HasPaid(),
                new MinecraftStatusCommand(),
                new WebsiteCommand(),
                new ChatbotCommand(),
                new TimeCommand(),
                new MirrorCommand(),
                new SteamStatusCommand(),
                new ToggleCommand(database, this),
                new KickCommand(),
                new BanCommand(database),
                new SendMessageCommand(),
                new ClearChatCommand(),
                new SpamCommand()
        );
    }

    public void registerCommands(Command... commands) {
        Collections.addAll(this.commands, commands);
    }

    public static boolean isCommandRequest(String room, String input) {
        String prefix = (String) database.getData(room).getSettings().get(Setting.PREFIX);

        return input.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public boolean isCommand(String command) {
        for (Command cmd : commands) {
            for (String name : cmd.getNames()) {
                if (name.equalsIgnoreCase(command)) return true;
            }
        }

        return false;
    }

    public Command getCommand(String command) {
        for (Command cmd : commands) {
            for (String name : cmd.getNames()) {
                if (name.equalsIgnoreCase(command)) return cmd;
            }
        }

        return null;
    }

    public void handle(CommandRequest request) {
        Scheduler.runAsyncTask(() -> handleCommand(request));
    }

    public void handleCommand(CommandRequest request) {
        String input = request.getInput();
        Room room = request.getRoom();

        Msg.debug("[Command handling] New command request!\ninput=" + input + "\nroom=" + room + "\nuser" + request.getUser());

        RoomSettings settings = database.getData(room.getId()).getSettings();
        String prefix = (String) settings.get(Setting.PREFIX);
        String noPrefix = input.substring(prefix.length());

        String[] args = noPrefix.split(" ");
        Msg.debug("[Command handling] args=" + Arrays.toString(args));
        String cmd = args[0];

        for (Command command : commands) {
            for (String name : command.getNames()) {
                if (name.equalsIgnoreCase(cmd)) {
                    @SuppressWarnings("unchecked")
                    List<String> disabledCommands = (List<String>) settings.get(Setting.DISABLED_COMMANDS);
                    if (disabledCommands.contains(command.getNames()[0].toLowerCase())) {
                        request.reply("That command is disabled! Enable it with the 'toggle' command.");
                        return;
                    }

                    Group group = permissions.getGroups(room.getId()).getUserGroup(request.getUser().getId());

                    if ((boolean) settings.get(Setting.SIMPLE_PERMS)) {
                        UserRole role = command.getRole();

                        switch (role) {
                            case ADMIN:
                                if (!group.getId().equalsIgnoreCase("admin") && !group.getId().equalsIgnoreCase("op")) {
                                    request.reply("You do not have permission to do that! (" + role.name().toUpperCase() + ")"
                                            + "\nYour role: " + group.getId()
                                            + "\nYour id: " + request.getUser().getId());
                                    return;
                                }
                                break;

                            case OP:
                                if (!group.getId().equalsIgnoreCase("op")) {
                                    request.reply("You do not have permission to do that! (" + role.name().toUpperCase() + ")"
                                            + "\nYour role: " + group.getId()
                                            + "\nYour id: " + request.getUser().getId());
                                    return;
                                }
                                break;
                        }
                    } else {
                        String node = command.getPermission();

                        if (!group.hasPermission(node)) {
                            request.reply("You do not have permission to do that! (" + node + ")"
                                    + "\nYour group: " + group.getId()
                                    + "\nYour id: " + request.getUser());
                            return;
                        }
                    }

                    boolean adminOnly = (boolean) settings.get(Setting.ADMIN_ONLY);

                    if (adminOnly && group.getId().equals("default")) {
                        request.reply("Room is in admin only mode! To disable this, use\nset admin-only false");
                        return;
                    }

                    List<String> newArgs = new ArrayList<>();
                    String firstArg = args[0];

                    for (String arg : args) {
                        if (!arg.equalsIgnoreCase(firstArg)) {
                            newArgs.add(arg);
                        }
                    }

                    int i = 0;
                    for (String s : args) {
                        Msg.debug("CMD> " + i + " " + s);
                        i++;
                    }

                    command.onCommand(request, newArgs.toArray(new String[newArgs.size()]));
                    return;
                }
            }
        }

        Msg.log(request.getUser() + " RAN THE UNKNOWN COMMAND '" + noPrefix + "'!");
    }

    public List<Command> getCommands() {
        return commands;
    }
}
