package com.darrelld.freeze;

import javax.swing.JFrame;

public class GUI extends JFrame {
	
	public GUI()
	{
		initUI();
	}
	
	private void initUI()
	{
        setTitle("Simple example");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
