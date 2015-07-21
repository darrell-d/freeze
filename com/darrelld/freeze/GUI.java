package com.darrelld.freeze;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.ScrollPane;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class GUI {

	private JFrame frmFreeze;
	private JTable table;

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
		frmFreeze.setBounds(100, 100, 850, 766);
		frmFreeze.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmFreeze.setJMenuBar(menuBar);
		
		JMenu mnAccount = new JMenu("Account");
		menuBar.add(mnAccount);
		
		JMenuItem mntmManageAccount = new JMenuItem("Manage Account");
		mnAccount.add(mntmManageAccount);
		
		JLabel lblRegions = new JLabel("Regions");
		lblRegions.setFont(new Font("Arial", Font.BOLD, 14));
		
		JSplitPane splitPane = new JSplitPane();
		
		JButton btnNewButton = new JButton("Upload");
		
		JButton btnNewButton_1 = new JButton("Download");
		
		JButton btnDelete = new JButton("Delete");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GroupLayout groupLayout = new GroupLayout(frmFreeze.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(tabbedPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
						.addComponent(splitPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
							.addComponent(lblRegions)
							.addGap(67)
							.addComponent(btnNewButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton_1)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDelete)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRegions)
						.addComponent(btnNewButton)
						.addComponent(btnNewButton_1)
						.addComponent(btnDelete))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(splitPane, GroupLayout.PREFERRED_SIZE, 245, GroupLayout.PREFERRED_SIZE)
					.addGap(28)
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 372, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(101, Short.MAX_VALUE))
		);
		
		ScrollPane scrollPane_2 = new ScrollPane();
		tabbedPane.addTab("Info", null, scrollPane_2, null);
		
		ScrollPane scrollPane_3 = new ScrollPane();
		tabbedPane.addTab("Log", null, scrollPane_3, null);
		
		ScrollPane scrollPane_1 = new ScrollPane();
		tabbedPane.addTab("Actions", null, scrollPane_1, null);
		
		JTree tree = new JTree();
		splitPane.setLeftComponent(tree);
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("All Regions") {
				{
					DefaultMutableTreeNode node_1;
					node_1 = new DefaultMutableTreeNode("US East");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("US West 1");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("US West 2");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("EU West");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("AP Southeast 1");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("AP Southeast 2");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("AP Northeast 1");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("SA East");
						node_1.add(new DefaultMutableTreeNode("-"));
					add(node_1);
				}
			}
		));
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		
		table = new JTable();
		table.setBorder(null);
		scrollPane.setViewportView(table);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"Test", "ZIP", "2MB", "10/24/2015"},
				{"Important", "DOCX", "1MB", "7/04/2014"},
			},
			new String[] {
				 "Filename", "Type", "Size", "Date modified"
			}
		));
		table.getColumnModel().getColumn(1).setPreferredWidth(113);
		table.getColumnModel().getColumn(3).setPreferredWidth(183);
		frmFreeze.getContentPane().setLayout(groupLayout);
	}

	public void setVisible(boolean b) {
		frmFreeze.setVisible(b);
		
	}
	public boolean isVisilble()
	{
		return frmFreeze.isVisible();
	}
}
