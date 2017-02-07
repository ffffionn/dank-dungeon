package com.test.test.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.test.test.DankDungeon;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = true;
		config.width = 800;
		config.height = 480;
		config.vSyncEnabled = true;
		new LwjglApplication(new DankDungeon(), config);
	}
}
