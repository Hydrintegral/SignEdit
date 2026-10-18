package hydrin.signedit.commands;

public enum Command {
    EDIT("edit", "<line> <text>", "Modifies the text on the specified line."),
    REPLACE("replace", "replace <oldText> <newText>", "Replaces all instances of a specific piece of text with another piece of text."),
    DYE("dye", "dye <dyeColor>", "Sets the dye color."),
    GLOW("glow", null, "Toggles the glow state."),
    WAX("wax", null, "Toggles the wax state."),
    CLICKEVENT_SET("clickevent set", "clickevent set <command>", "Sets the click event to run the specified command."),
    CLICKEVENT_CLEAR("clickevent clear", null, "Clears all click events."),
    TYPE("type", "type <signType>", "Sets the sign material to the specified type."),
    PRINT("print", null, "Prints copyable text of each line in chat."),
    HELP("help", "help", "Lists all commands and their functions.");

    private final String name;
    private final String usage;
    private final String description;

    Command(String name, String args, String description) {
        this.name = name;
        this.usage = args;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }
}
