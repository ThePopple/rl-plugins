package com.lucidplugins.lucidhotkeys2.api.util;

import net.runelite.api.widgets.Widget;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DialogUtils
{
    public static boolean hasOption(String option)
    {
        return hasOption(s -> s.equalsIgnoreCase(option));
    }

    public static boolean hasOption(Predicate<String> option)
    {
        return getOptions().stream().map(Widget::getText).filter(Objects::nonNull).anyMatch(option);
    }

    public static List<Widget> getOptions()
    {
        Widget widget = Static.getClient().getWidget(219, 1);
        if (!Widgets.isVisible(widget))
        {
            return Collections.emptyList();
        }
        else
        {
            List<Widget> out = new ArrayList();
            Widget[] children = widget.getChildren();
            if (children == null)
            {
                return out;
            }
            else
            {
                for (int i = 1; i < children.length; ++i)
                {
                    if (!children[i].getText().isBlank())
                    {
                        out.add(children[i]);
                    }
                }

                return out;
            }
        }
    }
}
