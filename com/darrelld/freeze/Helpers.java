package com.darrelld.freeze;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import freemarker.core.ParseException;

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
		System.out.println("freeze " + _VERSION);
		
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
	public static String parseZuluTime(Map<String, String> timeData)
	{
		DateFormat dateFormat = null;
		Date date = null;
		try
		{
			DateFormat utcFormat = new SimpleDateFormat(timeData.get("inputFormat"));
			utcFormat.setTimeZone(TimeZone.getTimeZone(timeData.get("inputTimeZone")));
	
			date = utcFormat.parse(timeData.get("payload"));
	
			dateFormat = new SimpleDateFormat(timeData.get("outputFormat"));
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeData.get("outputTimeZone")));
		}
		catch (java.text.ParseException e) 
		{
			e.printStackTrace();
		}
	
		return dateFormat.format(date);
	}
}
