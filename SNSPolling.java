import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;



public class SNSPolling {
	
	private AmazonGlacierClient client;
	private String vault;
	private String userID;
	private String region;
	private String jobID;
	
	 public static String sqsQueueName = "*** provide queue name **";
	
	public SNSPolling(AmazonGlacierClient client, String vaultName, String userID, String region) {
		this.client = client;
		this.vault = vaultName;
		this.userID = userID;
		this.region = region;
		jobID = "";
	
	}

	public String initRequest() {
		
		InitiateJobRequest initJobRequest = new InitiateJobRequest()
	    .withVaultName(vault)
	    .withJobParameters(
	            new JobParameters()
	                .withType("inventory-retrieval")
	                .withSNSTopic("arn:aws:glacier:"+ region +":"+ userID +":vaults/" + vault)
	      );

	InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
	jobID = initJobResult.getJobId();
	
	return jobID;
	}
	
	
	public void getResults()
	{
		GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest()
        .withVaultName(vault)
        .withJobId(jobID);
		GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);
		jobOutputResult.getBody();
	}
	
	
	

}
