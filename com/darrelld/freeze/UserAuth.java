package com.darrelld.freeze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

/*
 * Class for handling user authorization to AWS
 * 
 * */
public class UserAuth {
	private static AWSCredentials credentials;
	private static AmazonIdentityManagementClient iamClient;
	private static AmazonGlacierClient client;
	private static String userArn = "";
	private static String userID = "";
	private int region;

	private static final int _USERID_TOKEN_LOCATION = 4;
	private static final String _USERHOME = System.getProperty("user.home");
	
	private  Scanner scanner = new Scanner(System.in);
	
	
	UserAuth() throws FileNotFoundException
	{
		if(!doKeysExist())
    	{
    		createCredentials();
    	}
    	try {
			credentials = new PropertiesCredentials(new File(_USERHOME + "/awsCredentials.properties"));
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		iamClient = new AmazonIdentityManagementClient(credentials);
    	
    	//Get the userID
    	userArn = iamClient.getUser().getUser().getArn();    	
    	String[] tokens = userArn.split(":");
    	
    	//ARN takes the form arn:aws:iam::12-DIGIT-USERID:user/USERNAME
        userID = tokens[4];
        
        client = new AmazonGlacierClient(credentials);
        
        region = chooseRegion();        

        client.setEndpoint("https://glacier."+ Region.getRegion(Regions.values()[region]) +".amazonaws.com/");
	}
	
	//Check if keys exist
	private  boolean doKeysExist() {
		File f = new File(_USERHOME + "/awsCredentials.properties");
        
        
        if(!f.exists())
        {
        	return false;
        }
        
        return true;
	}
	
	//Create .properties file for keys
	private  void createCredentials()
	{
		System.out.println("No AWS security keys found. Please enter:");
    	System.out.print("secretKey = ");
    	String secretKey = scanner.nextLine();
    	System.out.print("accessKey = ");
    	String accessKey = scanner.nextLine();
    	
		try(FileWriter output = new FileWriter(_USERHOME + "/awsCredentials.properties")) 
		{			
	    	output.write("secretKey=" + secretKey);
	    	output.write("\r\n");
	    	output.write("accessKey=" + accessKey);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

    	
    	
	}
	
	//Print and choose region to upload to
    private  int chooseRegion()
    {
    	int glacierRegion = -1;
        System.out.println("Please choose your region:\r\n");
        int counter = 0;
        //Get region
        for(Regions r : Regions.values())
        {
        	System.out.println(counter++ + ": " +r );
        }
        try
        {
        	glacierRegion = scanner.nextInt();
        }
        catch(NumberFormatException e)
        {
        	System.out.println("Please use numerical inputs for your region selection.");
        	System.err.println(e);
        }
        finally{}
        return glacierRegion;
        

    }

	public AWSCredentials getCredentials() {
		return credentials;
	}


	public void setCredentials(AWSCredentials credentials) {
		UserAuth.credentials = credentials;
	}


	public AmazonIdentityManagementClient getIamClient() {
		return iamClient;
	}


	public void setIamClient(AmazonIdentityManagementClient iamClient) {
		UserAuth.iamClient = iamClient;
	}


	public AmazonGlacierClient getClient() {
		return client;
	}


	public  void setClient(AmazonGlacierClient client) {
		UserAuth.client = client;
	}


	public  String getUserArn() {
		return userArn;
	}


	public  void setUserArn(String userArn) {
		UserAuth.userArn = userArn;
	}


	public  String getUserID() {
		return userID;
	}


	public  void setUserID(String userID) {
		UserAuth.userID = userID;
	}


	public  int getUseridTokenLocation() {
		return _USERID_TOKEN_LOCATION;
	}
	
	public  int getRegion() {
		return region;
	}

	public  void setRegion(int region) {
		this.region = region;
	}


}
