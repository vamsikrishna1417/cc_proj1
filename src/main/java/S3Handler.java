import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;


public class S3Handler {

    private Regions clientRegion;
    private String bucketName;
    AmazonS3 s3Client;

    public S3Handler(Regions region, String name) throws AmazonServiceException, SdkClientException
    {
        clientRegion = region; //Regions.US_EAST_1
        bucketName = name; //"ccfoebucket";
        s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion)
            .build();
    }

    public void UploadVideo(String fileName) throws AmazonServiceException, SdkClientException
    {
        int index = fileName.lastIndexOf('\\');
        String keyName = fileName.substring(index + 1);

        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, new File(fileName));
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType("plain/text");
//        metadata.addUserMetadata("x-amz-meta-title", "someTitle");
//        request.setMetadata(metadata);

        s3Client.putObject(request);
    }

    public S3Object DownloadVideo(String keyName) throws AmazonServiceException, SdkClientException
    {
        S3Object fullObject;

        fullObject = s3Client.getObject(new GetObjectRequest(bucketName, keyName));

        return fullObject;
    }

}
