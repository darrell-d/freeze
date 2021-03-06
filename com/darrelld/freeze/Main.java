package com.darrelld.freeze;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
public class Main {

	private static final int MIN_ID_LENGTH = 4;
	private static final int _1KB = 1024;
    private static final int _1MB = 1048576;
    private static final int _1GB = 1073741824;
    private static final int _BASE64_FIX_POSITION = 14;
    private static final String _BASE64_STRIP_TEXT ="<m><v>2</v><p>";
    
    private static final int EXIT_SAFE = 0;
    private static final int NO_INPUT = 1;
    private static final int NO_COMMAND = 2;
    private static final int UNDEFINED_ERR = 99;
    
    private static int vault;
    private static int numArgs;
    
    private static String[] validCommands = {"upload","list", "download", "version"};
    private static HashMap<String, Boolean> switches = new HashMap<String,Boolean>();
	private static String vaultName = "";
	private static String command = "";
	private static String filePath = "";
	private static String userID = "";
	private static UserAuth auth;
	
	private static boolean isUsingGUI = false;
	
	private static Scanner scanner = new Scanner(System.in);
	
	
	private static List<DescribeVaultOutput> vaultList ;
	
	private static SNSPolling poll;
	
    public static void main(String[] args) throws IOException, ParseException {
    	
    	numArgs = args.length;
    	
    	if(numArgs == 0)
    	{
    		GUI freezeUI = new GUI();
        	freezeUI.setVisible(true);
    	}
    	else
    	{
	    	
	    	//Expect multiple switches but one command
	    	command = "";
	    	for(int i = 0; i < numArgs; i++)
	    	{
	    		if(args[i].charAt(0) == '-')
				{
	    			switches.put(args[i].substring(1,args[i].length()), true);
	    		}
	    		else
	    		{
	    			command = args[i];
	    	    	if(!Arrays.asList(validCommands).contains(command))
	    	    	{
	    	    		throw new UnsupportedOperationException(command + " is not a valid command");
	    	    	}
	    		}
	    	}
	    	
	    	if(command.contentEquals(""))
	    	{
	    		System.out.println(
	    				"No command passed\r\n" +
	    				"Current commands are list, upload and download \r\n");
	    		System.exit(NO_COMMAND);
	    	}
	    	
	    	
		    	switch(command.toLowerCase())
		    	{
					case "upload":
						filePath = args[1];
						authorize();
						upload();
						break;
					case "download":
						authorize();
						String id = args[1];
						download(id);
						break;
		    		case "list":
		    			authorize();
		    			list();
		    			break;
		    		case "version":
		    			Utilities.printVersion();
		    			break;
		    	}
    	}

    }

	private static void authorize() throws FileNotFoundException {
		auth = new UserAuth();
        
        vault = chooseArchive();
        
        vaultName = vaultList.get(vault).getVaultName();
	}


	//Print out a list of archives. Return int of selected archive
	private static int chooseArchive() {
		
		System.out.println("Choose your archive");
		
		String marker = null;
		   
		   do
		   {
			   ListVaultsRequest request = new ListVaultsRequest()
		  		.withLimit("25")
		  		.withMarker(marker);
			   
			   ListVaultsResult listVaultResults = auth.getClient().listVaults(request);
			   
			   vaultList = listVaultResults.getVaultList();
			   marker = listVaultResults.getMarker();
			   
			   int counter = 0;
			   for (DescribeVaultOutput vault : vaultList) {
			        System.out.println(counter + ": " + vault.getVaultName() + 
			        		", size:" + vault.getSizeInBytes()/_1GB + "GB, " + 
			        		"archives: " + vault.getNumberOfArchives());
			    }
			   
		   }while(marker!=null);
		   
		   return scanner.nextInt();
	}

	//Upload file to Glacier
	private static void upload() {
		try 
        {
            ArchiveTransferManager atm = new ArchiveTransferManager(auth.getClient(), auth.getCredentials());
            
            File uploadPayload = new File(filePath);
            System.out.println("uploading...");
            UploadResult result = atm.upload(vaultName, uploadPayload.getName(), uploadPayload);
            long filesize = uploadPayload.length();
            String modifier = "";
            if(filesize < _1MB)
            {
            	filesize = filesize / _1KB;
            	modifier = "KB";
            }
            else if(filesize > _1MB && filesize < _1GB )
            {
            	filesize = filesize / _1MB;
            	modifier = "MB";
            }
            else if(filesize > _1GB)
            {
            	filesize = filesize / _1GB;
            	modifier = "GB";
            }
            System.out.println("FileSize: " + filesize + modifier);
            System.out.println("Archive ID: " + result.getArchiveId());
            
        } catch (Exception e)
        {
            System.err.println(e);
        }
	}
	
	private static void download(String id)
	{
		if(id.length() < MIN_ID_LENGTH)
		{
			System.out.println("Please provide 4 or more characters in the ID");
			download(scanner.next());
		}
		ArchiveTransferManager atm = new ArchiveTransferManager(auth.getClient(), auth.getCredentials());
		HashMap<String,ArrayList<String>> fileHash = Utilities.createFileHash(vaultName, Region.getRegion(Regions.values()[auth.getRegion()]).getName());
		
		for (Entry<String, ArrayList<String>> e : fileHash.entrySet()) 
		{
			String s = e.getKey();
			if (s.startsWith(id))
			{
				System.out.println("File found requesting download");
				String archiveId = fileHash.get(e.getKey()).get(0);
				atm.download(vaultName, archiveId, new File("/freeze-downloads/" + fileHash.get(e.getKey()).get(1)));
			}
		}
	}
	
	
	private static void list() throws FileNotFoundException, IOException {
		//Initiate a SNS polling request
		setPoll(new SNSPolling(auth,vaultName,userID,Region.getRegion(Regions.values()[auth.getRegion()]).getName(), "us-east-1", "listFiles"));
		if(doesFileListExist())
		{
			listArchives();
		}
		else
		{
			generateArchiveListRequest();
		}
	}
	
	//List all existing Archives
	private static void generateArchiveListRequest() throws FileNotFoundException, IOException
	{		
		//auth.getClient() = new AmazonGlacierClient(credentials);
		auth.getClient().setEndpoint("https://glacier." + Region.getRegion(Regions.values()[auth.getRegion()]) + ".amazonaws.com");
        SNSPolling.sqsClient = new AmazonSQSClient(auth.getCredentials());
        SNSPolling.sqsClient.setEndpoint("https://sqs." + Region.getRegion(Regions.values()[auth.getRegion()])+ ".amazonaws.com");
        SNSPolling.snsClient = new AmazonSNSClient(auth.getCredentials());
        SNSPolling.snsClient.setEndpoint("https://sns." + Region.getRegion(Regions.values()[auth.getRegion()])+ ".amazonaws.com");
        
        try {
            SNSPolling.setupSQS();
            
            SNSPolling.setupSNS();

            String jobId = SNSPolling.initiateJobRequest();
            System.out.println("Jobid = " + jobId);
            
            Boolean success = SNSPolling.waitForJobToComplete(jobId, SNSPolling.sqsQueueURL);
            if (!success) 
            { 
            	throw new Exception("Job did not complete successfully."); 
        	}
            SNSPolling.downloadJobOutput(jobId);
            
            SNSPolling.cleanUp();
            
            //Exit 0 - success
            System.exit(EXIT_SAFE);
            
        } catch (Exception e) {
            System.err.println("Inventory retrieval failed.");
            System.err.println(e);
        } 
	}
	/*List out files which have already been retrieved from a archive list */
    private static void listArchives(){

    	try{
	    	String content = Utilities.readFile(SNSPolling.fileName, StandardCharsets.UTF_8);
	    	
			JSONArray jsonArray = new JSONObject(content).getJSONArray("ArchiveList");
			String inventoryDate_RAW = new JSONObject(content).getString("InventoryDate");
			HashMap<String,String>timeData = new HashMap<>();
			String timezone = "EST";
			
			timeData.put("inputFormat", "yyyy-MM-dd'T'HH:mm:ss'Z'");
			timeData.put("inputTimeZone", "UTC");
			timeData.put("outputFormat", "yyyy-MM-dd HH:mm:ss");
			timeData.put("outputTimeZone", timezone);
			timeData.put("payload", inventoryDate_RAW);
			
			String inventoryDate = Utilities.parseZuluTime(timeData);
			
			int listLength = jsonArray.length();
			
			for(int i = 0; i < listLength; i++)
			{
				String filename = jsonArray.getJSONObject(i).getString("ArchiveDescription");
				String archiveID = jsonArray.getJSONObject(i).getString("ArchiveId");
				//Check for a file sequence that some application uses to base64 encoding.
				if(filename.contains(_BASE64_STRIP_TEXT))
				{
					filename= filename.substring(_BASE64_FIX_POSITION,filename.indexOf("</p>"));
					filename = new String(Base64.decodeBase64(filename.getBytes()));
				}
				System.out.println(filename + " -- " + archiveID.subSequence(0, 4) );
			}
			
			System.out.println("\nInventory last updated " + inventoryDate + " " + timezone + ", would you like to update? Y/N");
			String result = scanner.next().toLowerCase();
			
			if(result.compareTo("y") == 0)
			{
				generateArchiveListRequest();
			}
			else
			{
				System.exit(EXIT_SAFE);
			}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
		
	}
	
	private static boolean doesFileListExist()
	{
		File f = new File(SNSPolling.fileName);
		File ff = new File("files/regions.txt");
		
		System.out.println(ff.exists());
		
		return f.exists();
	}

	public static SNSPolling getPoll() {
		return poll;
	}

	public static void setPoll(SNSPolling poll) {
		Main.poll = poll;
	}

	public static boolean isUsingGUI() {
		return isUsingGUI;
	}

	public static void setUsingGUI(boolean isUsingGUI) {
		Main.isUsingGUI = isUsingGUI;
	}
	
	
	
}
