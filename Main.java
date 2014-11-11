import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class Main {

    public static String vaultName = "MtgWorks";
    public static String archiveToUpload = "C:\\Users\\ddefreitas\\Downloads\\puttytel.exe";
    
    public static AmazonGlacierClient client;
	private static Scanner scanner;
    
    public static void main(String[] args) throws IOException {
        
        File f = new File(System.getProperty("user.home") + "/MyFile.properties");
        if(!f.exists())
        {
        	scanner = new Scanner(System.in);
        	System.out.print("secretKey = ");
        	String secretKey = scanner.nextLine();
        	System.out.print("accessKey = ");
        	String accessKey = scanner.nextLine();
        	FileWriter output = new FileWriter(System.getProperty("user.home") + "/MyFile.properties");
        	
        	output.write("secretKey=");
        	output.write(secretKey);
        	output.write("\r\n");
        	output.write("accessKey=");
        	output.write(accessKey);
        	
        	output.close();
        	
        	
        }
        AWSCredentials credentials = new PropertiesCredentials(new File(System.getProperty("user.home") + "/MyFile.properties"));
        client = new AmazonGlacierClient(credentials);
        client.setEndpoint("https://glacier.us-west-2.amazonaws.com/");

        try 
        {
            ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
            
            File uploadPayload = new File(archiveToUpload);
            UploadResult result = atm.upload(vaultName, "my archive " + (new Date()), uploadPayload);
            long filesize = uploadPayload.length();
            String modifier = "";
            if(filesize < 1048576)
            {
            	filesize = filesize / 1024;
            	modifier = "KB";
            }
            else if(filesize > 1048576 && filesize < 1073741824 )
            {//MB
            	filesize = filesize / 1048576;
            	modifier = "MB";
            }
            else if(filesize > 1073741824)
            {//GB
            	filesize = filesize / 1073741824;
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