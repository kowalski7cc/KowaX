package com.xspacesoft.kowax;

import com.xspacesoft.kowax.plugins.AppExample;
import com.xspacesoft.kowax.plugins.BusyBox;
import com.xspacesoft.kowax.plugins.CronTab;
import com.xspacesoft.kowax.plugins.DenialService;
import com.xspacesoft.kowax.plugins.Fortune;
import com.xspacesoft.kowax.plugins.HivemindControl;
import com.xspacesoft.kowax.plugins.Kalculator;
import com.xspacesoft.kowax.plugins.Kalendar;
import com.xspacesoft.kowax.plugins.KowaxUpdater;
import com.xspacesoft.kowax.plugins.Man;
import com.xspacesoft.kowax.windowsystem.kenvironment.KowaxDisplayManager;

public class DefaultPlugins {

	public static Object[][] getDefaults() {
		return new Object [][] {
				// ClassName, RootAccess
				{BusyBox.class, true},
				{CronTab.class, true},
				{AppExample.class, false},
				{HivemindControl.class, false},
				{DenialService.class, false},
				{Kalculator.class, false},
				{Kalendar.class, false},
				{Man.class, false},
				{Fortune.class, false},
				{KowaxDisplayManager.class, true},
				{KowaxUpdater.class, true},
			};
	}
}
