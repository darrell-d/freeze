package com.darrelld.simpleglacier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;


public class Main {

    private static final int _1MB = 1048576;
    private static final int _1GB = 1073741824;
    
    private static int region;
    private static int vault;
	private static String userHome = System.getProperty("user.home");
	private static String vaultName = "";
	private static String userID = "";
	private static Scanner scanner;
	private static AmazonGlacierClient client;
	private static List<DescribeVaultOutput> vaultList ;
	private static SNSPolling poll;
	private static AWSCredentials credentials;
	
	private static String action = "";
	private static String archiveToUpload = ""; // Test file
    

    public static void main(String[] args) throws IOException {
    	
    	switch(args[0].toLowerCase())
    	{
    		case "list":
    			listArchives();
    			break;
    		case "upload":
    			initUpload(args[1]);
    	}
    }

	private static void initUpload(String filePath) throws IOException
	{
		archiveToUpload = filePath;
    	credentials = new PropertiesCredentials(new File(System.getProperty("user.home") + "/MyFile.properties"));
        AmazonIdentityManagementClient iamClient = new AmazonIdentityManagementClient(credentials);
        
        String userArn = iamClient.getUser().getUser().getArn();
        String[] tokens = userArn.split(":");
        
        userID = tokens[4];
        
    	getKeys();
        region = chooseRegion();        

       client = new AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier."+ Region.getRegion(Regions.values()[region]) +".amazonaws.com/"); 
        
        System.out.println("What archive do you want to upload to?");
        vault = chooseArchive();
        vaultName = vaultList.get(vault).getVaultName();
        
        poll = new SNSPolling(client,vaultName,userID,Region.getRegion(Regions.values()[region]).getName(), "us-east-1", "Getiles");
        //listArchives();
        
        upload(credentials);
	}
    //Print out a list of archives. Return int of selected archive
	private static int chooseArchive() {
		String marker = null;
		   
		   do
		   {
			   ListVaultsRequest request = new ListVaultsRequest()
		  		.withLimit("25")
		  		.withMarker(marker);
			   
			   ListVaultsResult listVaultResults = client.listVaults(request);
			   
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

	//Check if credentials file exists, create if not.
	private static void getKeys() throws IOException {
		File f = new File(userHome + "/awsCredentials.properties");
        scanner = new Scanner(System.in);
        
        if(!f.exists())
        {
        	
        	System.out.print("secretKey = ");
        	String secretKey = scanner.nextLine();
        	System.out.print("accessKey = ");
        	String accessKey = scanner.nextLine();
        	
        	FileWriter output = new FileWriter(userHome + "/awsCredentials.properties");
        	
        	output.write("secretKey=" + secretKey);
        	output.write("\r\n");
        	output.write("accessKey=" + accessKey);
        	
        	output.close();
        	
        	
        }
	}
    
	//Print and choose region to upload to
    private static int chooseRegion()
    {
    	int glacierRegion = -1;
        System.out.println("Please choose the region to upload to:\r\n");
        int counter = 0;
        //Get region
        for(Regions r : Regions.values())
        {
        	System.out.println(counter++ + ": " +r + " " + r.getName());
        	
        }
        try
        {
        	glacierRegion = scanner.nextInt();
        }
        catch(NumberFormatException e)
        {
        	System.out.println("Please use numerical inputs for your region selection.");
        }
        finally
        {
        	
        }
        System.out.println("\r\n");
        return glacierRegion;
        

    }
    
    //Upload file to Glacier
	private static void upload(AWSCredentials credentials) {
		try 
        {
            ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
            
            File uploadPayload = new File(archiveToUpload);
            System.out.println("uploading...");
            UploadResult result = atm.upload(vaultName, uploadPayload.getName(), uploadPayload);
            long filesize = uploadPayload.length();
            String modifier = "";
            if(filesize < _1MB)
            {
            	filesize = filesize / 1024;
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
	private static void listArchives()
	{
		//Initiate a SNS polling request
		//TODO: You must wait to get poll information back from an archive list request. Make the poll but keep a list of known uploaded files.
		

        client = new AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier." + region + ".amazonaws.com");
        SNSPolling.sqsClient = new AmazonSQSClient(credentials);
        SNSPolling.sqsClient.setEndpoint("https://sqs." + Region.getRegion(Regions.values()[region])+ ".amazonaws.com");
        SNSPolling.snsClient = new AmazonSNSClient(credentials);
        SNSPolling.snsClient.setEndpoint("https://sns." + Region.getRegion(Regions.values()[region])+ ".amazonaws.com");
        
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
	
	
}