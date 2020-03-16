import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;

import java.util.List;

public class EC2Server {

    public static void main(String[] args)
    {
        Regions region = Regions.US_EAST_1;
        List<String> messagelist;
        try
        {
            SqsHandler sqhandle = new SqsHandler("input.fifo");
            S3Handler s3handle = new S3Handler(region, "ccfoebucket");

            messagelist = sqhandle.ReceiveMessage();
            System.out.println(messagelist.toString());

            // For each message received start instance
            for(String m: messagelist)
            {

            }

        }
        catch (AmazonServiceException e)
        {
            e.printStackTrace();
        }
        catch (SdkClientException e)
        {
            e.printStackTrace();
        }
    }
}
