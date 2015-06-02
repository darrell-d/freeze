package com.darrelld.freeze;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.aspectj.weaver.ast.HasAnnotation;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

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
	
	public static HashMap<String, ArrayList<String>> createFileHash(String vaultName, String region) throws JSONException
	{
		String filename = vaultName + "__" + region;
		String content = "";
		JSONArray jsonArray = null;
		ArrayList<String> payload = new ArrayList<String>();
		String key = null;
		HashMap<String,ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
		
		try 
		{
			content = readFile(filename, StandardCharsets.UTF_8);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		try 
		{
			jsonArray = new JSONObject(content).getJSONArray("ArchiveList");
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		
		
		for(int i = 0; i < jsonArray.length(); i ++)
		{
			key = jsonArray.getJSONObject(i).getString("SHA256TreeHash");
			payload.add(jsonArray.getJSONObject(i).getString("ArchiveId"));
			payload.add(jsonArray.getJSONObject(i).getString("ArchiveDescription"));
			payload.add(jsonArray.getJSONObject(i).getString("CreationDate"));
			payload.add(jsonArray.getJSONObject(i).getString("Size"));
			payload.add(jsonArray.getJSONObject(i).getString("SHA256TreeHash"));
			
			h.put(key, payload);
			
		}
		System.out.println(h.get("327bc240a670c89b8b301177e741496d92108666906900455e123fa780fcf35a").get(0));
		
		return h;
	}
}
