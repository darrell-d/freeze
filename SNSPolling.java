import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;


public class SNSPolling {
	
	private AmazonGlacierClient client;
	private String vaultName;
	private String userID;
	private String region;
	
	public SNSPolling(AmazonGlacierClient client, String vaultName, String userID, String region) {
		this.client = client;
		this.vaultName = vaultName;
		this.userID = userID;
		this.region = region;		
	
	}

	public String initRequest() {
		
		InitiateJobRequest initJobRequest = new InitiateJobRequest()
	    .withVaultName(vaultName)
	    .withJobParameters(
	            new JobParameters()
	                .withType("inventory-retrieval")
	                .withSNSTopic("arn:aws:glacier:"+ region +":"+ userID +":vaults/" + vaultName)
	      );

	InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
	String jobId = initJobResult.getJobId();
	
	return jobId;
	}
	
	
	

}
