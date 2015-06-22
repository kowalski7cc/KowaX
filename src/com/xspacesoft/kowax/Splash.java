package com.xspacesoft.kowax;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class Splash extends JWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = -459660910713560886L;
	private JPanel contentPane;
	private JLabel lblLogwolf;
	private JProgressBar progressBar;
	private JPanel panel_1;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Splash frame = new Splash();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Splash() {
		setName("Starting up...");
		setType(Type.UTILITY);
		setWindowPosition(this);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setForeground(Color.BLACK);
		panel.setBackground(Color.WHITE);
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		lblLogwolf = new JLabel(Core.SHELLNAME + " " + Core.VERSION);
		lblLogwolf.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblLogwolf.setBackground(Color.WHITE);
		lblLogwolf.setForeground(Color.BLACK);
		panel.add(lblLogwolf, BorderLayout.SOUTH);
		
		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);
		
		JLabel lblPoweredByXspacesoftcom = new JLabel("Powered by XSpaceSoft.com");
		lblPoweredByXspacesoftcom.setBounds(10, 11, 171, 14);
		panel_1.add(lblPoweredByXspacesoftcom);
		
		JLabel lblVersion = new JLabel("Beta");
		lblVersion.setVerticalAlignment(SwingConstants.TOP);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersion.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 15));
		lblVersion.setBounds(382, 11, 48, 25);
		panel_1.add(lblVersion);
		
		JLabel lblLogo = new JLabel("");
		lblLogo.setIcon(new ImageIcon(Splash.class.getResource("/com/xspacesoft/kowax/Splash.png")));
		lblLogo.setBounds(0, 0, 440, 262);
		panel_1.add(lblLogo);
	}

	@Override
	public void setVisible(boolean b) {
		if(b) {
			setOpacity(0);
			super.setVisible(b);
			setWindowPosition(this);
		} else {
			super.setVisible(b);
		}
	}
	
	public void fadeIn() {
		try {
			setOpacity(0);
			for(Float i=(float) 0;i.floatValue()<=1;i=(float) (i+0.02)) {
				Thread.sleep(5);
				setOpacity(i);
			}
			setOpacity(1);
		} catch (InterruptedException e) {
			setOpacity(1);
			super.setVisible(true);
		}
	}
	
	public void fadeOut() {
		try {
			setOpacity(1);
			for(Float i=(float) 1;i.floatValue()>=0;i=(float) (i-0.02)) {
				Thread.sleep(5);
				setOpacity(i);
			}
			setOpacity(0);
		} catch (InterruptedException e) {
			setOpacity(1);
			super.setVisible(true);
		}
	}

	public JLabel getLblLogwolf() {
		return lblLogwolf;
	}
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	private void setWindowPosition(JWindow jWindow) {        
		// get the size of the screen, on systems with multiple displays,		
		// the primary display is used
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// calculate the new location of the window
		int w = jWindow.getSize().width;
		int h = jWindow.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		// moves this component to a new location, the top-left corner of
		// the new location is specified by the x and y
		// parameters in the coordinate space of this component's parent
		jWindow.setLocation(x, y);
	}
}
