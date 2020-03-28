import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;


import java.util.List;

public class EC2Server {

	public BasicSessionCredentials basicSessionCredentials(){
		return new BasicSessionCredentials(
				"ASIA3HIKMVVQDMRLQ5BT",
				"URYoTx3pH77b/oty0AoktL21YTxQ395NLeQ1vvje", "FwoGZXIvYXdzEM3//////////wEaDMW2XNwoB2v171ZkwiK/AaBiSXMDPRk1jFF1roapgdXx8UnFp6tkEhyp06t/+OYcfkXHEGMihiMdnZyKwp/G5VI58UhXixxTUUI+A6ed/a0/jDentSy1XT+2sC6UXkxYzRjVhCnmPdPUySHS+pKNswHXbbYEKWQFAwcvd2SqN2YJ3pxGfTL3cNmjijGMnaSIyDDtqIzIoL9ucFb4sWw59yOtHR1xnVTEVXIMs1Lv9Zuwskc/H81sRLJenvXMicKMEzJm4gJ8NDyo60sUhgdFKKeT+/MFMi3cDRTZFiDWJGtRWDvS+rbm1rMVIOzGAoC6M6zedzlqDkZ8E086ZCmSoZ3K80g=");
	}
    // private List<AmazonEC2> ec2clientList;
    private AmazonEC2 ec2Client;
    private String ami_id;
    private String key_pair;
    private static LinkedList<String> instanceIDs;
    public EC2Server()
    {
    	ec2Client = createEC2Client();
    	ami_id = "ami-0903fd482d7208724";
        key_pair = "ccproj";
		instanceIDs = new LinkedList<>();
    	// int numofInstances = CreateInstance(ec2Client, 3, -1, ami_id, key_pair);
        // Create 2 instances
        // for(int i=1;i<3;i++){
        // 	String instanceId = CreateInstance(ec2Client, i);
        // 	ec2clientList.add(ec2Client);
        // 	instanceIDs.add(instanceId);
        // }
        
        

    }

    public int startInstances(int count, int minCount, SqsHandler handler){

        return CreateInstance(count, minCount, handler);
    }


    public static void main(String[] args)
    {
//        EC2Server server = new EC2Server();
//        SqsHandler sqsHandler = new SqsHandler("instance.fifo");
//        server.startInstance("i-0c79eea5149bc8cd9");
    	try {
			EC2Server server = new EC2Server();
			//AmazonEC2 ec2 = server.createEC2Client();
			//server.CreateInstance(ec2, 1, 0);
			SqsHandler sqsHandler = new SqsHandler("instance.fifo", "0");
//			server.startInstance("i-0ef52c42544bf2479");
		}
    	catch (AmazonServiceException e)
		{
			e.printStackTrace();
		}
        //server.startInstance("i-00b179da287023d3c");
//        server.stopInstance("i-08102f449e453d1d4");
//        int count = server.startInstances(2,1, sqsHandler);
//        System.out.println("Count: "+count);
//		sqsHandler.SendMessage("i-0400fdc340c8957c0", "1", 0);
//		sqsHandler.SendMessage("i-0472b3ec7814e3335", "1", 0);
//        Regions region = Regions.US_EAST_1;
//        List<String> messagelist;
//        try
//        {
//            SqsHandler sqhandle = new SqsHandler("input.fifo");
//            S3Handler s3handle = new S3Handler(region, "ccfoebucket");
//
////            messagelist = sqhandle.ReceiveMessage();
////            System.out.println(messagelist.toString());
//
//            // For each message received start instance
////            for(String m: messagelist)
////            {
////
////            }
//
//        }
//        catch (AmazonServiceException e)
//        {
//            e.printStackTrace();
//        {
//        }
//        catch (SdkClientException e)
//            e.printStackTrace();
//        }
    }

    public AmazonEC2 createEC2Client(){

		BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
				"ASIA3HIKMVVQBT3JKNHX",
				"Ach96l9jsmYGioaCyPu0ajkal28ruUGqND2aUUB2",
				"FwoGZXIvYXdzEMn//////////wEaDGXzpV8+8oLC9MtVDSK/AdNPaN7kn/PWFtFG01uB+YDr1wf52Q+Ac0kbXn5F/gFOrIInlBXDHqwMoXZUAIDNT40y4avu0QzgUuVusspEQmVQgTRhh96ZpPB5jE5XJrEr2euRGNwJnPjStIjl7V4uIJZyMplP07otPPCwYtcngJ8g2ayGTW8RPflkqTdhsk6NycA9TgN+zahdatd19y5hObJYy5+K56U7cj6hxI5NmhCKshIITflvzgr49CTcoUwPdNGrHVJfIY/KuMqt3bgjKKGf+vMFMi1JK4GHyooez7Ddut08RnHE7Xz9CV+Rix15YYi31Cb2HXjWMJeIfI8s9xDr6ZY=");


    	AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
    					.withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
    					.withRegion(Regions.US_EAST_1)
    					.build();
    	return ec2;
    }

    public int CreateInstance(int maxNumberInstance, int count, SqsHandler sqsHandler) throws AmazonServiceException, SdkClientException
    {
    	int minInstance = maxNumberInstance -1;
    	int maxInstance = maxNumberInstance;
    	if(minInstance == 0)
    		minInstance = 1;
    	List<String> securityIds = new ArrayList<>();
    	securityIds.add("*******");
    	List<TagSpecification> tagSpecsList = new ArrayList<>();
    	TagSpecification tagSpec = new TagSpecification();
    	List<Tag> tagList = new ArrayList<>();
    	Tag tag = new Tag();
    	tag.setKey("Name");
    	tag.setValue("EC2 - App");
    	tagList.add(tag);
    	tagSpec.setResourceType("instance");
    	tagSpec.setTags(tagList);
    	tagSpecsList.add(tagSpec);

    	List<String> securityGroupId = new ArrayList<>();
    	securityGroupId.add("sg-0e8845c279230e932");
        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType("t2.micro")
                .withMaxCount(maxInstance)
                .withMinCount(minInstance)
                .withKeyName(key_pair);

        run_request.setSecurityGroupIds(securityGroupId);

        RunInstancesResult run_response = null;
        try{
        	run_response = ec2Client.runInstances(run_request);
			String instanceId = run_response.getReservation().getInstances().get(0).getInstanceId();
			instanceIDs.addLast(instanceId);
			sqsHandler.SendMessage(instanceId, "1", 0);
			stopInstance(instanceId);
        } catch(AmazonEC2Exception amzec2Exp){
            //System.out.println("AmazonEC2Exception ");
			amzec2Exp.printStackTrace();
        	return count;
        } catch(Exception e){
            //System.out.println("General Exception ");
			e.printStackTrace();
        	return count;
        }
        // RunInstancesResult run_response = ec2Client.runInstances(run_request);
        // String instanceId = instanceResult.getReservation().getInstances().get(0).getInstanceId();
        
        //I'll associate a tag for the resource, so that we can identify resource with tag name later
        // CreateTagsRequest createTagsRequest = new CreateTagsRequest()
        //         .withResources(instanceId)
        //         .withTags(new Tag("ec2client"+number, "ec2client"+number));
        // ec2Client.createTags(createTagsRequest);
        return maxNumberInstance;
    }

    public void startInstance(String instanceId){
    	StartInstancesRequest startRequest = new StartInstancesRequest().withInstanceIds(instanceId);
    	ec2Client.startInstances(startRequest);
    }

    public void stopInstance(String instanceId){
    	StopInstancesRequest stopRequest = new StopInstancesRequest().withInstanceIds(instanceId);
    	ec2Client.stopInstances(stopRequest);
    }

    public void terminateInstance(String instanceId){
    	TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
    	ec2Client.terminateInstances(terminateRequest);
    }

    public DescribeInstanceStatusResult describeInstanceStatus(DescribeInstanceStatusRequest instanceRequest){
    	return ec2Client.describeInstanceStatus(instanceRequest);
    }

    public int getNumberOfInstances(){
    	DescribeInstanceStatusRequest describeRequest = new DescribeInstanceStatusRequest();
    	describeRequest.setIncludeAllInstances(true);
    	DescribeInstanceStatusResult describeInstances = describeInstanceStatus(describeRequest);
    	List<InstanceStatus> instanceStatusList = describeInstances.getInstanceStatuses();
    	int liveInstancesCount=0;
    	for(InstanceStatus instanceStatus: instanceStatusList){
    		InstanceState instanceState = instanceStatus.getInstanceState();
    		if(InstanceStateName.Running.toString().equals(instanceState.getName())){
    			liveInstancesCount++;
    		}
    	}
    	return liveInstancesCount;
    }
}
