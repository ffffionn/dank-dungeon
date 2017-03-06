package com.test.test.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.test.test.DankDungeon;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = true;
		config.width = 1024;
		config.height = 768;
		config.vSyncEnabled = true;
//		config.fullscreen = true;
		new LwjglApplication(new DankDungeon(), config);
	}
}
