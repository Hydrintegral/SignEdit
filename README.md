# SignEdit

SignEdit is a simple, but advanced sign editor paper plugin with built in [PlotSquared](https://www.spigotmc.org/resources/plotsquared-v7.77506/) and [CoreProtect](https://modrinth.com/plugin/coreprotect)* integration, and more!

## Commands

Each command targets the sign face you're looking at:

| Command                                 | Description                                                                                          | Permission            |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------- | --------------------- |
| `/signedit line <text>`                 | Modifies the text on a specified line.                                                               | `signedit.edit`       |
| `/signedit replace <oldText> <newText>` | Replaces all instances of a specific piece of text with another specific piece of text.              | `signedit.replace`    |
| `/signedit dye <dyeColor>`              | Sets the dye color.                                                                                  | `signedit.dye`        |
| `/signedit glow`                        | Toggles the glow state.                                                                              | `signedit.glow`       |
| `/signedit wax`                         | Toggles the wax state.                                                                               | `signedit.wax`        |
| `/signedit clickevent set <command>`    | Sets the click event to run the specified command.                                                   | `signedit.clickevent` |
| `/signedit clickevent clear`            | Clears all click events.                                                                             | `signedit.clickevent` |
| `/signedit type <signType>`             | Sets the sign material to the specified type.                                                        | `signedit.type`       |
| `/signedit print`                       | Prints copyable text of each line in chat.                                                           | `signedit.print`      |

## Formatting

Both legacy formatting and [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) is supported, with legacy being converted to MiniMessage automatically.

SignEdit also allows for customizable formatting permissions:

| Type          | Formats                                                                     | Permission                    |
| ------------- | --------------------------------------------------------------------------- | ----------------------------- |
| Color         | The standard 16 colors and RGB colors.                                      | `signedit.formatting.color`   |
| Shadow        | Modified shadow text color.                                                 | `signedit.formatting.shadow`  |
| Style         | Bold, strikethrough, underline and italic text.                             | `signedit.formatting.style`   |
| Magic         | Obfuscated text.                                                            | `signedit.formatting.magic`   |
| Heads         | Player head sprites.                                                        | `signedit.formatting.heads`   |
| Sprites       | Texture atlas sprites.                                                      | `signedit.formatting.sprites` |
| Miscellaneous | Keybinds, translation keys, selector patternsm and scoreboard and NBT data. | `signedit.formatting.misc`    |

Font and reset tags are permitted by default.

## Notes

\* CoreProtect's API currently lacks any way of logging sign text changes by official means, however, it will still log the sign being replaced, which should be more than enough information.
