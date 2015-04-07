import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

import com.amazonaws.regions.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class Main {

    private static final int _1MB = 1048576;
    private static final int _1GB = 1073741824;
    
    private static int region;
    private static int vault;
	private static String userHome = System.getProperty("user.home");
	private static String vaultName = "";
	private static String userID = "";
	private static String archiveToUpload = "C:\\Users\\ddefreitas\\Downloads\\mtgworks_v5.sql"; // Test file
	private static Scanner scanner;
	private static AmazonGlacierClient client;
	private static List<DescribeVaultOutput> vaultList ;
	private static SNSPolling poll;
    

    public static void main(String[] args) throws IOException {
    	
    	AWSCredentials credentials = new PropertiesCredentials(new File(System.getProperty("user.home") + "/MyFile.properties"));
        AmazonIdentityManagementClient iamClient = new AmazonIdentityManagementClient(credentials);
        
        String userArn = iamClient.getUser().getUser().getArn();
        String[] tokens = userArn.split(":");
        
        userID = tokens[4];
        
    	getKeys();
        region = chooseRegion();        

       client = new AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier."+ Region.getRegion(Regions.values()[region]) +".amazonaws.com/"); 
        
        System.out.println("Please choose your archive:");
        vault = chooseArchive();
        vaultName = vaultList.get(vault).getVaultName();
        
        //poll = new SNSPolling(client,vaultName,userID,regionName,vault);
        
        upload(credentials);
    }

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

	private static void getKeys() throws IOException {
		//Check if credentials file exists, create if not.
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
	
}