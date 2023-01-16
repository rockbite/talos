package com.talosvfx.talos;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import java.lang.String;
import java.lang.System;
import java.util.Calendar;
import java.util.Date;
import lombok.Getter;

public final class TALOS_BUILD {
  @Getter
  private static final String version = "2.0.0-SNAPSHOT";

  @Getter
  private static final String commit = "4fcb37a";

  @Getter
  private static final String branch = "snapshot-version";

  @Getter
  private static final Date buildDate = new Date(1673899027931l);

  private static void printHeader() {
    System.out.println("      ___           ___           ___       ___           ___     \n"
            + "     /\\  \\         /\\  \\         /\\__\\     /\\  \\         /\\  \\    \n"
            + "     \\:\\  \\       /::\\  \\       /:/  /    /::\\  \\       /::\\  \\   \n"
            + "      \\:\\  \\     /:/\\:\\  \\     /:/  /    /:/\\:\\  \\     /:/\\ \\  \\  \n"
            + "      /::\\  \\   /::\\~\\:\\  \\   /:/  /    /:/  \\:\\  \\   _\\:\\~\\ \\  \\ \n"
            + "     /:/\\:\\__\\ /:/\\:\\ \\:\\__\\ /:/__/    /:/__/ \\:\\__\\ /\\ \\:\\ \\ \\__\n"
            + "    /:/  \\/__/ \\/__\\:\\/:/  / \\:\\  \\    \\:\\  \\ /:/  / \\:\\ \\:\\ \\/__/\n"
            + "   /:/  /           \\::/  /   \\:\\  \\    \\:\\  /:/  /   \\:\\ \\:\\__\\  \n"
            + "   \\/__/            /:/  /     \\:\\  \\    \\:\\/:/  /     \\:\\/:/  /  \n"
            + "                   /:/  /       \\:\\__\\    \\::/  /       \\::/  /   \n"
            + "                   \\/__/         \\/__/     \\/__/         \\/__/    \n");
  }

  private static void printDate() {
    Calendar cal = Calendar.getInstance();
    System.out.println("Current Date: " + cal.getTime().toString());
  }

  private static void printVersion() {
    System.out.println("Runtime Version: " + version);
    System.out.println("Runtime commit: " + commit);
    System.out.println("Runtime branch: " + branch);
  }

  private static void printSystemInfo() {
    boolean is64Bit = SharedLibraryLoader.is64Bit;
    Application.ApplicationType applicationType = Gdx.app.getType();
    System.out.println("Platform: " + applicationType.name());
    System.out.println("Is 64 Bit: " + is64Bit);
    String glVersion = Gdx.graphics.getGLVersion().getDebugVersionString();
    System.out.println(glVersion);
  }

  public static void printAll() {
    printHeader();
    printDate();
    printVersion();
    printSystemInfo();
  }
}
