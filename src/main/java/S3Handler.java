import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.*;


public class S3Handler {

    private Regions clientRegion;
    private String bucketName;
    AmazonS3 s3Client;

    public S3Handler(Regions region, String name) throws AmazonServiceException, SdkClientException
    {
//        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey,
//                Credentials.secretKey, Credentials.sessionKey);
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Credentials.accessKey, Credentials.secretKey);
        clientRegion = region; //Regions.US_EAST_1
        bucketName = name; //"ccfoebucket";
        s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion).withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .build();
    }

    public void createBucket() throws AmazonServiceException, SdkClientException
    {
         if(!s3Client.doesBucketExistV2(bucketName))
         {
                 s3Client.createBucket(new CreateBucketRequest(bucketName));
                 String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
                 System.out.println("Bucket Created in location: " + bucketLocation);
         }
         else
         {
             System.out.println(bucketName + " Already Exists");
         }

    }
    public void Upload(String fileName) throws AmazonServiceException, SdkClientException
    {
        int index = fileName.lastIndexOf('/');
        String keyName = fileName.substring(index + 1);

        System.out.println("Upload to S3 " + keyName);
        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, new File(fileName));
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType("plain/text");
//        metadata.addUserMetadata("x-amz-meta-title", "someTitle");
//        request.setMetadata(metadata);

        s3Client.putObject(request);
    }

    public int Download(String keyName, String downloadPath) throws AmazonServiceException, SdkClientException
    {
        S3Object fullObject;

        System.out.println("Download from S3 " + keyName);
        fullObject = s3Client.getObject(new GetObjectRequest(bucketName, keyName));
        File f = new File(downloadPath + keyName);

        try {
            InputStream reader = new BufferedInputStream(fullObject.getObjectContent());
            OutputStream writer = new BufferedOutputStream(new FileOutputStream(f));

            int read = -1;

            while ((read = reader.read()) != -1)
            {
                writer.write(read);
            }

            writer.flush();
            writer.close();
            reader.close();
        }
        catch(AmazonS3Exception e)
        {
            if(e.getErrorCode() == "NoSuchKey")
                return 0;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

}
