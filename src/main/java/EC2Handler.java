import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

public class EC2Handler {

    // private AmazonEC2 ec2;
    // private String ami_id;
    // private String key_pair;
    // private String[] InstanceIDs;
    public EC2Handler()
    {
        // ec2 = AmazonEC2ClientBuilder.defaultClient();
        // ami_id = "ami-0903fd482d7208724";
        // key_pair = "ccproj";
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

    public void StopInstance() throws AmazonServiceException, SdkClientException
    {
//        StopInstancesRequest request = new StopInstancesRequest()
//                .withInstanceIds(InstanceIDs[0]);
//
//        ec2.stopInstances(request);
    }
}
