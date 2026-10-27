# SignEdit

SignEdit is a simple, but advanced sign editor paper plugin with built in [PlotSquared](https://www.spigotmc.org/resources/plotsquared-v7.77506/) and [CoreProtect](https://modrinth.com/plugin/coreprotect) integration and more!

## Commands

Commands that modify signs will target the sign face that you're looking at:

| Command                                 | Description                                                                             | Permission            |
|-----------------------------------------|-----------------------------------------------------------------------------------------|-----------------------|
| `/signedit <line> <text>`               | Modifies the text on the specified line.                                                | `signedit.edit`       |
| `/signedit replace <oldText> <newText>` | Replaces all instances of a specific piece of text with another specific piece of text. | `signedit.replace`    |
| `/signedit dye <dyeColor>`              | Sets the dye color.                                                                     | `signedit.dye`        |
| `/signedit glow`                        | Toggles the glow state.                                                                 | `signedit.glow`       |
| `/signedit wax`                         | Toggles the wax state.                                                                  | `signedit.wax`        |
| `/signedit clickevent set <command>`    | Sets the click event to run the specified command.                                      | `signedit.clickevent` |
| `/signedit clickevent clear`            | Clears all click events.                                                                | `signedit.clickevent` |
| `/signedit type <signType>`             | Sets the sign material to the specified type.                                           | `signedit.type`       |
| `/signedit print`                       | Prints copyable text of each line in chat.                                              | `signedit.print`      |
| `/signedit help`                        | Lists all commands and their functions.                                                 | `signedit.help`       |

## Formatting

Both legacy formatting and [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) are supported, with legacy being converted to MiniMessage automatically.

Additionaly, a MineSkin tag exists to insert a player head sprite sourced from [MineSkin](https://mineskin.org/), the syntax for it is as follows:

`<mineskin:id[:outer_layer]>`

`id`: the ID trailing a MineSkin skin URL.

`outer_layer`: either `true` or `false` and determines whether the hat layer should be drawn. Defaults to `true`.

## Formatting Permissions

SignEdit also allows for customizable formatting permissions:

| Type          | Formats                                                                     | Permission                     |
|---------------|-----------------------------------------------------------------------------|--------------------------------|
| Color         | The standard 16 colors and RGB colors.                                      | `signedit.formatting.color`    |
| Shadow        | Modified shadow text color.                                                 | `signedit.formatting.shadow`   |
| Style         | Bold, strikethrough, underline and italic text.                             | `signedit.formatting.style`    |
| Magic         | Obfuscated text.                                                            | `signedit.formatting.magic`    |
| Heads         | Player head sprites.                                                        | `signedit.formatting.heads`    |
| MineSkin      | Custom player head sprites from MineSkin.                                   | `signedit.formatting.mineskin` |
| Sprites       | Texture atlas sprites.                                                      | `signedit.formatting.sprites`  |
| Miscellaneous | Keybinds, translation keys, selector patterns, and scoreboard and NBT data. | `signedit.formatting.misc`     |

Font and reset tags are permitted by default.
    