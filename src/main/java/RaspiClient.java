import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;

import java.io.IOException;

public class RaspiClient {

    public static void main(String[] args)
    {
        Regions region = Regions.US_EAST_1;
        String filepath= "C:\\Spring20\\clipcanvas_14348_offline.mp4";
        try
        {
            SqsHandler sqhandle = new SqsHandler("input.fifo");
            S3Handler s3handle = new S3Handler(region, "ccfoebucket");

            s3handle.UploadVideo(filepath);
            int ind = filepath.lastIndexOf('\\');
            String filename = filepath.substring(ind + 1);

            sqhandle.SendMessage(filename, "uploadvid");
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
