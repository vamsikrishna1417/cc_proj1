import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

public class EC2Handler {

     private AmazonEC2 ec2;
     private String ami_id;
     private String key_pair;
     private String InstanceID;

    public EC2Handler()
    {

        ami_id = "ami-0903fd482d7208724";
        key_pair = "ccproj";

        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey, Credentials.secretKey, Credentials.sessionKey);
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        // InstanceIDs = new String[] {"i-052e0682aa7e279db", " ", " ", " ", " ", " ", " ", " ",
        //         " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "};


    }
    // public void CreateInstance() throws AmazonServiceException, SdkClientException
    // {

    //     RunInstancesRequest run_request = new RunInstancesRequest()
    //             .withImageId(ami_id)
    //             .withInstanceType(InstanceType.T1Micro)
    //             .withMaxCount(1)
    //             .withMinCount(1)
    //             .withKeyName(key_pair);

    //     RunInstancesResult run_response = ec2.runInstances(run_request);
    // }

    public void StartInstance() throws AmazonServiceException, SdkClientException
    {
//        StartInstancesRequest request = new StartInstancesRequest()
//                .withInstanceIds(InstanceIDs[0]);
//        ec2.startInstances(request);

    }

    public void StopInstance(String instanceID) throws AmazonServiceException, SdkClientException
    {
        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instanceID);

        ec2.stopInstances(request);
    }
}
