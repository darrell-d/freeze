package com.darrelld.freeze;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
		frmFreeze.setBounds(100, 100, 850, 592);
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
					.addGap(18)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
					.addGap(29))
		);
		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("New tab", null, tabbedPane_1, null);
		
		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("New tab", null, tabbedPane_2, null);
		
		final JTree tree = new JTree();
		splitPane.setLeftComponent(tree);
		
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("All Regions") {
				{
					DefaultMutableTreeNode node_1;
					try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("regions.txt"))) ) {
					    String line;
					    while ((line = br.readLine()) != null) {
					    	node_1 = new DefaultMutableTreeNode(line);
					    	node_1.add(new DefaultMutableTreeNode("X"));
					    	add(node_1);
					    }
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		));
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
				
				//Return if nothing is selected
				if(node == null) return;
				
				//Retrieve selected node
				Object nodeInfo = node.getUserObject();
				
				//TODO: Change archive listing
			}
		});

		
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
