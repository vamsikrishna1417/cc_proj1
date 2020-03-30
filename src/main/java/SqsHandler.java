import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqsHandler {
//    static {
        // Your accesskey and secretkey
    BasicAWSCredentials AWS_CREDENTIALS = new BasicAWSCredentials(
                "ASIA3HIKMVVQNLLAURHS",
                "JSuu7yX706QsioLEhGUzHpDBn1/0IsQEemdr6ecU"
        );
//    }
    private AmazonSQS sqs;
    private String sqsName;
    private String sqsUrl;

    public SqsHandler(String name) throws AmazonServiceException, SdkClientException
    {
//        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey,
//                Credentials.secretKey, Credentials.sessionKey);
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Credentials.accessKey, Credentials.secretKey);
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        sqsName = name;
        try {
            sqsUrl = sqs.getQueueUrl(sqsName).getQueueUrl();
            System.out.println("Got: " + sqsUrl);
        }
        catch (AmazonSQSException e)
        {
            if(e.getErrorCode() == "NonExistentQueue")
            {
                System.out.println("queue doesn't exist");
                sqsUrl = "";
            }
        }
//        final SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest()
//                .withQueueUrl(sqsUrl)
//                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20")
//                .addAttributesEntry("DelaySeconds", delaySeconds);
//        sqs.setQueueAttributes(setQueueAttributesRequest);
    }

    public void createQueue()
    {
        try {
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(sqsName);
            String queueUrl = sqs.createQueue(createQueueRequest)
                    .getQueueUrl();
            System.out.println(queueUrl + " Queue Created");
        }
        catch (AmazonSQSException e)
        {
            if(e.getErrorCode() == "QueueAlreadyExists")
            {
                System.out.println(sqsName + " QueueAlreadyExists");
            }
        }
    }

    public void SendMessage(String message, int delayInSeconds) throws AmazonServiceException, SdkClientException
    {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(sqsUrl)
                .withMessageBody(message)
                 .withDelaySeconds(delayInSeconds);

        sqs.sendMessage(send_msg_request);
    }

    public void deleteMessage(Message message){
        String messageReciptHandle = message.getReceiptHandle();
        DeleteMessageRequest deleteRequest = new DeleteMessageRequest(sqsUrl, messageReciptHandle);
//        System.out.println("Msg Body: "+message.getBody());
        sqs.deleteMessage(deleteRequest);
    }

    public void SendBatchMessage(String[] message) throws AmazonServiceException, SdkClientException
    {
        SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest();
        List<SendMessageBatchRequestEntry> requestEntryList = new ArrayList<>();
        send_batch_request.withQueueUrl(sqsUrl);

        for(int i=0; i<message.length; i++)
        {
            requestEntryList.add(new SendMessageBatchRequestEntry(Integer.toString(i), message[i]));
        }

        send_batch_request.withEntries(requestEntryList);
        sqs.sendMessageBatch(send_batch_request);
    }

    public Message ReceiveMessage() throws AmazonServiceException, SdkClientException
    {
        List<String> messagelist = new ArrayList<>();
        final ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
            .withQueueUrl(sqsUrl)
            .withMaxNumberOfMessages(1)
            .withWaitTimeSeconds(5)
                .withVisibilityTimeout(10)
                .withWaitTimeSeconds(20);
        List<Message> messagesList = sqs.receiveMessage(receive_request).getMessages();
        if(messagesList.isEmpty())
            return null;
        // Delete messages from sqs
        // for (Message m : messages) {
        //     sqs.deleteMessage(sqsUrl, m.getReceiptHandle());
        // }

        // for(Message m: messages)
        // {
        //     messagelist.add(m.getBody());
        // }

        return messagesList.get(0);
    }

    public int  getApproximateMessageCount(){
        List<String> attributesNamesList = new ArrayList<>();
        attributesNamesList.add("ApproximateNumberOfMessages");
        GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(sqsUrl, attributesNamesList);
        Map map = sqs.getQueueAttributes(getQueueAttributesRequest).getAttributes();
        Integer messageCount = Integer.valueOf((String)map.get("ApproximateNumberOfMessages"));
//        System.out.println(messageCount);
        return messageCount;
    }
}
