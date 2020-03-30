import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
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

    // private List<AmazonEC2> ec2clientList;
    private AmazonEC2 ec2Client;
    private String ami_id;
    private String key_pair;
    private S3Handler s3Input;
    private S3Handler s3Output;
    private SqsHandler sqsInputQueue;
    private SqsHandler sqsInstanceQueue;
    private static LinkedList<String> instanceIds;

    public EC2Server()
    {
    	ec2Client = createEC2Client();
    	// This ami has client jars and darknet shell script alongwith darkent app
    	ami_id = "ami-06a411207fb0fbcdc"; //"ami-0903fd482d7208724";
        key_pair = "cc_proj1_latest";
		instanceIds = new LinkedList<>();
		s3Input = new S3Handler(Regions.US_EAST_1, "ccproj1inputbucket");
		s3Output = new S3Handler(Regions.US_EAST_1, "ccproj1outputbucket");
		sqsInputQueue = new SqsHandler("inputqueue");
		sqsInstanceQueue = new SqsHandler("instancequeue");
    }

	public void createS3Buckets()
	{
		s3Input.createBucket();
		s3Output.createBucket();
	}

	public void createSQSQueues()
	{
		sqsInputQueue.createQueue();
		sqsInstanceQueue.createQueue();
	}

    public static void main(String[] args)
    {
//		LoadBalancer loadBalancer = new LoadBalancer();
    	try {
			EC2Server server = new EC2Server();
			server.createS3Buckets();
			server.createSQSQueues();
			server.CreateInstance(9);
			server.scaleInAndOut();
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

	public void scaleInAndOut(){
		int count=0;

		while(true){
			int messageCount = sqsInputQueue.getApproximateMessageCount();
			int ec2ClientInstances = getNumberOfInstances()-1; //bcoz master is counted as an instance
			if(messageCount>0 && messageCount>ec2ClientInstances){
				int maxLimitInstances = 19 - ec2ClientInstances;
				if(maxLimitInstances>0){
					int messagesToBeServed = messageCount - ec2ClientInstances;
					int countOfAvailableInstances = sqsInstanceQueue.getApproximateMessageCount();
					if(messagesToBeServed >= maxLimitInstances){
						while(countOfAvailableInstances != 0){
							Message instanceId = sqsInstanceQueue.ReceiveMessage();
							if(instanceId != null){
								sqsInstanceQueue.deleteMessage(instanceId);
								startInstance(instanceId.getBody());
								countOfAvailableInstances--;
							}else{
//								break; // No available instances.
							}
						}
					}else{
						if(messagesToBeServed >= countOfAvailableInstances){
							while(countOfAvailableInstances != 0){
								Message instanceId = sqsInstanceQueue.ReceiveMessage();
								if(instanceId != null){
									sqsInstanceQueue.deleteMessage(instanceId);
									startInstance(instanceId.getBody());
									countOfAvailableInstances--;
								}else{
//									break; // No available instances
								}
							}
						}else{
							while(messagesToBeServed != 0){
								Message instanceId = sqsInstanceQueue.ReceiveMessage();
								sqsInstanceQueue.deleteMessage(instanceId);
								startInstance(instanceId.getBody());
								messagesToBeServed--;
							}
						}
					}
				}
			}
			try{
				Thread.sleep(3000);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

    public AmazonEC2 createEC2Client(){
//		BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey,
//				Credentials.secretKey, Credentials.sessionKey);
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Credentials.accessKey, Credentials.secretKey);
		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
    					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
    					.withRegion(Regions.US_EAST_1)
    					.build();
    	return ec2;
    }

    public void CreateInstance(int maxNumberInstance) throws AmazonServiceException, SdkClientException
    {
    	int minInstance = maxNumberInstance -1;
    	int maxInstance = maxNumberInstance;
    	String id;

    	if(minInstance == 0)
    		minInstance = 1;

    	List<String> securityGroupId = new ArrayList<>();
    	securityGroupId.add("sg-0f504548d8788d95f");
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
			List<Instance> runningInstances= run_response.getReservation().getInstances();
			for(Instance i: runningInstances)
			{
				id = i.getInstanceId();
				sqsInstanceQueue.SendMessage(id, 0);
				instanceIds.add(id);
			}

			Thread.sleep(35000);
			System.out.println("Running instances count: " + getNumberOfInstances());
			for(String ID: instanceIds)
			{
				//stopInstance(ID);
			}
        } catch(AmazonEC2Exception amzec2Exp){
            //System.out.println("AmazonEC2Exception ");
			amzec2Exp.printStackTrace();

        } catch(Exception e){
            //System.out.println("General Exception ");
			e.printStackTrace();

        }
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
