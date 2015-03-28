package com.xspacesoft.kowax;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TrayHelper {
	
	private final SystemTray tray = SystemTray.getSystemTray();
	private TrayIcon trayIcon;
	private int port;
	private String password = "Your password";

	public TrayHelper(int port) {
        this.port = port;
	}
	
	public void addTray() {
		if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException();
        }
		String path = "/com/xspacesoft/kshell/tray.gif";
        Image img = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(path));
        PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(img);
        MenuItem modeHint = new MenuItem("Project security");
        modeHint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO add window
              }
            });
		String myAddress;
		try {
			myAddress = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			myAddress = "127.0.0.1";
//			Initrfs.log.e("Error getting server ip (TrayIcon");
		}
		String address = new String(myAddress + ":" + port);
		String passwordHint = new String("Password:");
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTray();
				System.exit(0);
			}
		});
		popup.add(modeHint);
		popup.addSeparator();
		popup.add(address);
		popup.add(passwordHint);
		popup.add(password);
		popup.addSeparator();
		popup.add(exit);
		trayIcon.setPopupMenu(popup);
		trayIcon.setToolTip("SOIC Dashboard");
		try {
			tray.add(trayIcon);
		} catch (NullPointerException e) {
//			Initrfs.log.e("Error finding image in jar. Is package damaged?");
		} catch (AWTException e) {
//			Initrfs.log.e("Error adding tray icon");
		} catch (Exception e) {
//			Initrfs.log.e("Error finding image in jar. Is package damaged?");
		}
	}
	
	public void removeTray() {
		tray.remove(trayIcon);
	}
}
