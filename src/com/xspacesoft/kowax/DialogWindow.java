package com.xspacesoft.kowax;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;

public class DialogWindow extends JFrame {

	public enum DialogType {
		ERROR,
		WARNING,
		INFO,
	}
	
	private static final long serialVersionUID = 1472685476206493968L;
	private JPanel contentPane;
//	private String title;
//	private String text;
//	private DialogType type;

	public static void show(final String title, final String text, final DialogType type) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DialogWindow frame = new DialogWindow(title, text, type);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private DialogWindow(String title, String text, DialogType type) {
		switch(type) {
		case ERROR: title = "Error:" + title;
			break;
		case INFO: title = "Info:" + title;
			break;
		case WARNING: title = "Warning:" + title;
			break;
		}
		setTitle(title);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 434, 137);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		JLabel lblJimage = new JLabel("JImage");
		contentPane.add(lblJimage, BorderLayout.WEST);
		JLabel lblJlabel = new JLabel(text);
		contentPane.add(lblJlabel, BorderLayout.CENTER);
		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener()  {
			@Override
			public void actionPerformed(ActionEvent e) {
				DialogWindow.this.setVisible(false);
			}
		});
		contentPane.add(btnOk, BorderLayout.SOUTH);
	}

}
