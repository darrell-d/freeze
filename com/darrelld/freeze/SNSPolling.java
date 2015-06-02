package com.darrelld.freeze;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;



public class SNSPolling {
	
	private static UserAuth userAuth;
	public static AmazonSQSClient sqsClient;
	public static AmazonSNSClient snsClient;
	
	public static String sqsQueueARN;	
	public static String sqsQueueURL;
	private static String sqsQueueName;
	private static String snsTopicName;
	public static String snsTopicARN;
	public static String snsSubscriptionARN;
	public static String fileName = "";
	
	private static String vault;
	private String userID;
	private String region;

	private String jobID;
	
	public static final long _SLEEPTIME = 600; 
	private static final long _10MINS = _SLEEPTIME * 1000;
	
	
	
	public SNSPolling(UserAuth auth, String vaultName, String userID, String region, String queueName,String topicName) {
		userAuth = auth;
		this.setVault(vaultName);
		this.setUserID(userID);
		this.setRegion(region);
		this.setSqsQueueName(queueName);
		this.setSnsTopicName(topicName);
		jobID = "";
		fileName = vaultName + "__" + region;
	
	}

	//Initiate a request. Requires AWS Glacier ARN to be created (jobID)
	public static String initiateJobRequest() {
        
        JobParameters jobParameters = new JobParameters()
            .withType("inventory-retrieval")
            .withSNSTopic(snsTopicARN);
        
        InitiateJobRequest request = new InitiateJobRequest()
            .withVaultName(getVault())
            .withJobParameters(jobParameters);
        
        InitiateJobResult response = userAuth.getClient().initiateJob(request);
        return response.getJobId();
    }
	
	public static void setupSQS() {
	        CreateQueueRequest request = new CreateQueueRequest()
	            .withQueueName(getSqsQueueName());
	        CreateQueueResult result = sqsClient.createQueue(request);  
	        sqsQueueURL = result.getQueueUrl();
	                
	        GetQueueAttributesRequest qRequest = new GetQueueAttributesRequest()
	            .withQueueUrl(sqsQueueURL)
	            .withAttributeNames("QueueArn");
	        
	        GetQueueAttributesResult qResult = sqsClient.getQueueAttributes(qRequest);
	        sqsQueueARN = qResult.getAttributes().get("QueueArn");
	        
	        Policy sqsPolicy = 
	            new Policy().withStatements(
	                    new Statement(Effect.Allow)
	                    .withPrincipals(Principal.AllUsers)
	                    .withActions(SQSActions.SendMessage)
	                    .withResources(new Resource(sqsQueueARN)));
	        Map<String, String> queueAttributes = new HashMap<String, String>();
	        queueAttributes.put("Policy", sqsPolicy.toJson());
	        sqsClient.setQueueAttributes(new SetQueueAttributesRequest(sqsQueueURL, queueAttributes)); 

    }
	
	public static void setupSNS() {
        CreateTopicRequest request = new CreateTopicRequest()
            .withName(getSnsTopicName());
        CreateTopicResult result = snsClient.createTopic(request);
        snsTopicARN = result.getTopicArn();

        SubscribeRequest request2 = new SubscribeRequest()
            .withTopicArn(snsTopicARN)
            .withEndpoint(sqsQueueARN)
            .withProtocol("sqs");
        SubscribeResult result2 = snsClient.subscribe(request2);
                
        snsSubscriptionARN = result2.getSubscriptionArn();
    }
    
	public static Boolean waitForJobToComplete(String jobId, String sqsQueueUrl) throws InterruptedException, JsonParseException, IOException {
        
        Boolean messageFound = false;
        Boolean jobSuccessful = false;
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        
        while (!messageFound) {
        	System.out.print(".");
            List<Message> msgs = sqsClient.receiveMessage(
               new ReceiveMessageRequest(sqsQueueUrl).withMaxNumberOfMessages(10)).getMessages();

            if (msgs.size() > 0) {
                for (Message m : msgs) {
                    JsonParser jpMessage = factory.createJsonParser(m.getBody());
                    JsonNode jobMessageNode = mapper.readTree(jpMessage);
                    String jobMessage = jobMessageNode.get("Message").getTextValue();
                    
                    JsonParser jpDesc = factory.createJsonParser(jobMessage);
                    JsonNode jobDescNode = mapper.readTree(jpDesc);
                    String retrievedJobId = jobDescNode.get("JobId").getTextValue();
                    String statusCode = jobDescNode.get("StatusCode").getTextValue();
                    if (retrievedJobId.equals(jobId)) {
                        messageFound = true;
                        if (statusCode.equals("Succeeded")) {
                            jobSuccessful = true;
                        }
                    }
                }
                
            } else {
              Thread.sleep(_10MINS); 
            }
          }
        return (messageFound && jobSuccessful);
    }
    
    public static void downloadJobOutput(String jobId) throws IOException {

        GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
            .withVaultName(getVault())
            .withJobId(jobId);
        GetJobOutputResult getJobOutputResult = userAuth.getClient().getJobOutput(getJobOutputRequest);
    
        FileWriter fstream = new FileWriter(fileName);
        BufferedWriter out = new BufferedWriter(fstream);
        BufferedReader in = new BufferedReader(new InputStreamReader(getJobOutputResult.getBody()));            
        String inputLine;
        try {
            while ((inputLine = in.readLine()) != null) {
                out.write(inputLine);
            }
        }catch(IOException e) {
            throw new AmazonClientException("Unable to save archive", e);
        }finally{
            try 
            {
            	in.close();
        	}  
            catch (Exception e) 
            {
            	e.printStackTrace();
            }
            try 
            {
            	out.close();
        	}  
            catch (Exception e) 
            {
            	e.printStackTrace();
            }             
        }
        System.out.println("Retrieved inventory to " + fileName);
    }
    
    public static void cleanUp() {
        snsClient.unsubscribe(new UnsubscribeRequest(snsSubscriptionARN));
        snsClient.deleteTopic(new DeleteTopicRequest(snsTopicARN));
        sqsClient.deleteQueue(new DeleteQueueRequest(sqsQueueURL));
    }
	
	
	public void getResults()
	{
		GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest()
        .withVaultName(getVault())
        .withJobId(jobID);
		GetJobOutputResult jobOutputResult = userAuth.getClient().getJobOutput(jobOutputRequest);
		jobOutputResult.getBody();
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public static String getVault() {
	
		return vault;
	}

	public void setVault(String vault) {
		SNSPolling.vault = vault;
	}

	public static String getSqsQueueName() {
		return sqsQueueName;
	}

	public void setSqsQueueName(String sqsQueueName) {
		this.sqsQueueName = sqsQueueName;
	}

	public static String getSnsTopicName() {
		return snsTopicName;
	}

	public void setSnsTopicName(String snsTopicName) {
		this.snsTopicName = snsTopicName;
	}
	
	
	

}
