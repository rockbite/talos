package com.talosvfx.talos.editor.notifications;

import com.talosvfx.talos.editor.notifications.commands.enums.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandEventHandler {
    Commands.CommandType commandType();
}
