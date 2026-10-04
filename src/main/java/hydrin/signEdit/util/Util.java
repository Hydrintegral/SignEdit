package hydrin.signEdit;

import net.kyori.adventure.text.Component;

public class Util {
    public static Component stripClickEvents(Component component) {
        if (component.clickEvent() != null) {
            component = component.clickEvent(null);

            for (int i = 0; i < component.children().size(); i++) {
                component = stripClickEvents(component.children().get(i));
            }
        }

        return component;
    }
}
