package com.darrelld.freeze;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Window.Type;

public class GUI {

	private JFrame frmFreeze;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmFreeze.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFreeze = new JFrame();
		frmFreeze.setTitle("Freeze");
		frmFreeze.setBounds(100, 100, 850, 850);
		frmFreeze.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmFreeze.setJMenuBar(menuBar);
		
		JMenu mnAccount = new JMenu("Account");
		menuBar.add(mnAccount);
		
		JMenuItem mntmManageAccount = new JMenuItem("Manage Account");
		mnAccount.add(mntmManageAccount);
	}

	public void setVisible(boolean b) {
		frmFreeze.setVisible(b);
		
	}
	public boolean isVisilble()
	{
		return frmFreeze.isVisible();
	}

}
