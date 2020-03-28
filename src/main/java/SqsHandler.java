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

    public SqsHandler(String name, String delaySeconds) throws AmazonServiceException, SdkClientException
    {
        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(Credentials.accessKey,
                Credentials.secretKey, Credentials.sessionKey);
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        sqsName = name;
        sqsUrl = sqs.getQueueUrl(sqsName).getQueueUrl();
        final SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest()
                .withQueueUrl(sqsUrl)
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20")
                .addAttributesEntry("DelaySeconds", delaySeconds);
        sqs.setQueueAttributes(setQueueAttributesRequest);
    }

    public CreateQueueResult createQueue(String queueName, AmazonSQS sqs){
        return sqs.createQueue(queueName);
    }

    public void SendMessage(String message, String groupID, int delayInSeconds) throws AmazonServiceException, SdkClientException
    {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(sqsUrl)
                .withMessageBody(message);
                //.withDelaySeconds(delayInSeconds);

        send_msg_request.setMessageGroupId(groupID);
                //.withDelaySeconds(5);
        sqs.sendMessage(send_msg_request);
    }

    public void deleteMessage(Message message){
        String messageReciptHandle = message.getReceiptHandle();
        DeleteMessageRequest deleteRequest = new DeleteMessageRequest(sqsUrl, messageReciptHandle);
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
                .withVisibilityTimeout(10);
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

    public int getApproximateMessageCount(){
        List<String> attributesNamesList = new ArrayList<>();
        attributesNamesList.add("ApproxiamteNumberOfMessages");
        GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(sqsUrl, attributesNamesList);
        Map<String, String> map = sqs.getQueueAttributes(getQueueAttributesRequest).getAttributes();
        Integer messageCount = Integer.valueOf(map.get("ApproxiamteNumberOfMessages"));
        return messageCount;
    }
}
