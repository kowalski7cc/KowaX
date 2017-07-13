package com.xspacesoft.kowax;
import com.xspacesoft.kowax.kernel.MacroManager;
import com.xspacesoft.kowax.plugins.AppExample;
import com.xspacesoft.kowax.plugins.BusyBox;
import com.xspacesoft.kowax.plugins.CronTab;
import com.xspacesoft.kowax.plugins.Echo;
import com.xspacesoft.kowax.plugins.Escalator;
import com.xspacesoft.kowax.plugins.Fortune;
import com.xspacesoft.kowax.plugins.HivemindControl;
import com.xspacesoft.kowax.plugins.Kalculator;
import com.xspacesoft.kowax.plugins.Kalendar;
import com.xspacesoft.kowax.plugins.KowaxExplorer;
import com.xspacesoft.kowax.plugins.No;
import com.xspacesoft.kowax.plugins.ServiceManager;
import com.xspacesoft.kowax.plugins.Yes;
import com.xspacesoft.kowax.telegram.TelegramServer;
import com.xspacesoft.kowax.windowsystem.kenvironment.KowaxDisplayManager;

public final class DefaultPlugins {

	public static Object[][] getDefaults() {
		return new Object [][] {
				// ClassName, RootAccess, AutostartService, Run@Boot, 
				{BusyBox.class, true, true, true},
				{CronTab.class, true, true, true},
				{AppExample.class, false, false, false},
				{HivemindControl.class, false, true, true},
//				{DenialService.class, false, false, false},
				{Kalculator.class, false, false, false},
				{Kalendar.class, false, false, false},
//				{Man.class, false, false, false},
				{Fortune.class, false, false, false},
				{KowaxDisplayManager.class, true, true, true},
//				{KowaxUpdater.class, true, true, true},
				{ServiceManager.class, true, false, false},
				{Escalator.class, false, false, false},
				{MacroManager.class, false, false, false},
				{KowaxExplorer.class, true, false, false},
				{TelegramServer.class, true, true, true},
				{Yes.class, false, false, false},
				{No.class, false, false, false},
				{Echo.class, false, false, false},
			};
	}
}
