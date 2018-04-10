package com.xspacesoft.kowax;
import com.xspacesoft.kowax.plugins.Echo;
import com.xspacesoft.kowax.plugins.Fortune;
import com.xspacesoft.kowax.plugins.Kalculator;
import com.xspacesoft.kowax.plugins.Kalendar;
import com.xspacesoft.kowax.plugins.KowaBox;
import com.xspacesoft.kowax.plugins.KowaxExplorer;
import com.xspacesoft.kowax.plugins.PLoaderTest;
import com.xspacesoft.kowax.plugins.PluginsList;
import com.xspacesoft.kowax.plugins.Screenfetch;
import com.xspacesoft.kowax.plugins.UserServiceManager;

public final class DefaultPlugins {
	
	public final static Class<?>[] defaultPlugins = new Class<?>[] {
		KowaBox.class,
		Kalculator.class,
		Kalendar.class,
		Fortune.class,
		UserServiceManager.class,
		KowaxExplorer.class,
		Echo.class,
		Screenfetch.class,
		PluginsList.class,
		PLoaderTest.class
	};

//	public static Object[][] getDefaults() {
//		return new Object [][] {
//				// ClassName, RootAccess, AutostartService, Run@Boot, 
//				{KowaBox.class, true, true, true},
//				{PLoaderTest.class, true, true, true},
//				{Kalculator.class, false, false, false},
//				{Kalendar.class, false, false, false},
//				{Fortune.class, false, false, false},
//				{UserServiceManager.class, true, false, false},
//				{MacroManager.class, false, false, false},
//				{KowaxExplorer.class, true, false, false},
//				{Echo.class, false, false, false},
//				{Screenfetch.class, false, false, false},
//			};
//	}
}
