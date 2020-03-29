import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EC2Handler {

     private AmazonEC2 ec2Client;
     private String ami_id;
     private String key_pair;

    public EC2Handler()
    {
        ami_id = "ami-0903fd482d7208724";
        key_pair = "ccproj";

        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey, Credentials.secretKey, Credentials.sessionKey);
        ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
     public LinkedList<String> CreateInstance(int minNumberInstance, int maxNumberInstance) throws AmazonServiceException, SdkClientException
     {

         int minInstance = minNumberInstance; //maxNumberInstance -1;
         int maxInstance = maxNumberInstance;
         LinkedList<String> instanceIDList = new LinkedList<>();

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
             List<Instance> instanceList = run_response.getReservation().getInstances();
             for (Instance i: instanceList)
             instanceIDList.addLast(i.getInstanceId());

         } catch(AmazonEC2Exception amzec2Exp){
             amzec2Exp.printStackTrace();
             return null;
         } catch(Exception e){
             
             e.printStackTrace();
             return null;
         }

         return instanceIDList;
     }

    public void StartInstance(String instanceID) throws AmazonServiceException, SdkClientException
    {
        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instanceID);
        ec2Client.startInstances(request);

    }

    public void StopInstance(String instanceID) throws AmazonServiceException, SdkClientException
    {
        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceID);

        ec2Client.stopInstances(request);
    }
}
