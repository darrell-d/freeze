package com.darrelld.simpleglacier;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Helpers {
	private static final String _VERSION = "v0.02";

	//Read a file and return text as a string
	public static String readFile(String path, Charset encoding) throws IOException 
		{
		  byte[] encoded = Files.readAllBytes(Paths.get(path));
		  return new String(encoded, encoding);
		}

	//Print current version
	public static void printVersion() {
		System.out.println(_VERSION);
		
	}
	
	public static String getVersion() {
		return _VERSION;
		
	}
	
	public static boolean expect(String expected, String actual)
	{
		expected = expected.toLowerCase();
		actual = actual.toLowerCase();
		
		if(expected.compareTo(actual) == 0)
		{
			return true;
		}
		
		return false;
	}
}
