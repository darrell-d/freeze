package com.darrelld.simpleglacier;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.AWSCredentials;
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

	private static final int _1KB = 1024;
    private static final int _1MB = 1048576;
    private static final int _1GB = 1073741824;
    private static final int _BASE64_FIX_POSITION = 14;
    private static final String _BASE64_STRIP_TEXT ="<m><v>2</v><p>";
    
    private static int vault;
    private static int numArgs;
    
    private static String[] validCommands = {"upload","list"};
	private static String vaultName = "";
	private static String command = "";
	private static String filePath = "";
	private static String userID = "";
	private static UserAuth auth;
	
	private static List<String>flags = new ArrayList<String>();
	
	private static Scanner scanner = new Scanner(System.in);
	
	
	private static List<DescribeVaultOutput> vaultList ;
	
	private static SNSPolling poll;
	
    public static void main(String[] args) throws IOException {
    	/*
    	 * Expected form of arguments is 'freeze <command> /file/location -otherArgs'
    	 * Current commands are list and upload
    	 * -otherArgs will take the form: -arg"data" or -arg 
    	 * */
    	
    	numArgs = args.length;
    	
    	if(numArgs == 0)
    	{
    		
    		URL classURL = Main.class.getResource("instructions");
    		String path = classURL.getPath();
    		path = path.substring(1,path.length());
    		String instructions = Helpers.readFile(path, StandardCharsets.UTF_8);
    		System.out.println(instructions);
    		return;
    	}
    	
    	command = args[0];
    	
    	if(!Arrays.asList(validCommands).contains(command))
    	{
    		throw new UnsupportedOperationException(command + " is not a valid command");
    	}
    	
    	for(int i = 2; i < args.length; i++)
    	{
    		flags.add(args[i]);
    	}
    	auth = new UserAuth();
        
        vault = chooseArchive();
        
        vaultName = vaultList.get(vault).getVaultName();
    	
    	switch(command.toLowerCase())
    	{
			case "upload":
				filePath = args[1];
				upload(auth.getCredentials());
				break;
    		case "list":
    			if(doesFileListExist())
    			{
    				listArchives();
    			}
    			else
    			{
    				generateArchiveListRequest();
    			}
    			break;
    	}
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
	private static void upload(AWSCredentials credentials) {
		try 
        {
            ArchiveTransferManager atm = new ArchiveTransferManager(auth.getClient(), credentials);
            
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
	
	//List all existing Archives
	private static void generateArchiveListRequest() throws FileNotFoundException, IOException
	{
		//Initiate a SNS polling request
		setPoll(new SNSPolling(auth,vaultName,userID,Region.getRegion(Regions.values()[auth.getRegion()]).getName(), "us-east-1", "listFiles"));
		
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
            if (!success) { throw new Exception("Job did not complete successfully."); }
            
            SNSPolling.downloadJobOutput(jobId);
            
            SNSPolling.cleanUp();
            
        } catch (Exception e) {
            System.err.println("Inventory retrieval failed.");
            System.err.println(e);
        } 
	}
	/*List out files which have already been retrieved from a archive list */
    private static void listArchives(){

    	try{
	    	String content = Helpers.readFile(SNSPolling.fileName, StandardCharsets.UTF_8);
	    	
			JSONArray jsonArray = new JSONObject(content).getJSONArray("ArchiveList");
			
			int listLength = jsonArray.length();
			
			for(int i = 0; i < listLength; i++)
			{
				String filename = jsonArray.getJSONObject(i).getString("ArchiveDescription");
				//Check for a file sequence that some application uses to base64 encoding.
				if(filename.contains(_BASE64_STRIP_TEXT))
				{
					filename= filename.substring(_BASE64_FIX_POSITION,filename.indexOf("</p>"));
					filename = new String(Base64.decodeBase64(filename.getBytes()));
				}
				System.out.println(filename);
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
		
		return f.exists();
	}

	public static SNSPolling getPoll() {
		return poll;
	}

	public static void setPoll(SNSPolling poll) {
		Main.poll = poll;
	}
	
	
	
}
