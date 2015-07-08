package com.darrelld.freeze;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.Component;
import java.awt.ScrollPane;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;

public class fGUI {

	private JFrame frame;
	private JTextField txtLeft;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					fGUI window = new fGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public fGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("Account");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmAddAccount = new JMenuItem("Add Account");
		mnNewMenu.add(mntmAddAccount);
		
		JMenuItem mntmFile = new JMenuItem("Manage Accounts");
		mnNewMenu.add(mntmFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnNewMenu.add(mntmExit);
		
		JList list = new JList();
		list.setModel(new AbstractListModel() {
			String[] values = new String[] {"A", "B", "V"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frame.getContentPane().add(list, BorderLayout.NORTH);
		
		JSplitPane splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JTextPane txtpnRight = new JTextPane();
		txtpnRight.setText("Right");
		splitPane.setRightComponent(txtpnRight);
		
		txtLeft = new JTextField();
		txtLeft.setText("Left");
		splitPane.setLeftComponent(txtLeft);
		txtLeft.setColumns(10);
	}

}
