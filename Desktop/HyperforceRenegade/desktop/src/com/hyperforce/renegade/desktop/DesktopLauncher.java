package com.hyperforce.renegade.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.hyperforce.renegade.MainClass;
import com.hyperforce.renegade.Secret;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 768;
		config.height = 768;
		new LwjglApplication(new MainClass(), config);
	}
}