package de.cozmic.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.cozmic.Game;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = 800;
        config.height = 600;
        config.resizable = false;
        config.title = "Roboxery";
        config.vSyncEnabled = true;
        config.addIcon("icon.png", Files.FileType.Internal);

        new LwjglApplication(new Game(), config);
    }
}
