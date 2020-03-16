import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.ArrayList;
import java.util.List;

public class SqsHandler {
    private AmazonSQS sqs;
    private String sqsName;
    private String sqsUrl;

    public SqsHandler(String name) throws AmazonServiceException, SdkClientException
    {
        sqs = AmazonSQSClientBuilder.defaultClient();
        sqsName = name;
        sqsUrl = sqs.getQueueUrl(sqsName).getQueueUrl();
        final SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest()
                .withQueueUrl(sqsUrl)
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
        sqs.setQueueAttributes(setQueueAttributesRequest);
    }

    public void SendMessage(String message, String groupid) throws AmazonServiceException, SdkClientException
    {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(sqsUrl)
                .withMessageBody(message);

        send_msg_request.setMessageGroupId(groupid);
                //.withDelaySeconds(5);
        sqs.sendMessage(send_msg_request);
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

    public List<String> ReceiveMessage() throws AmazonServiceException, SdkClientException
    {
        List<String> messagelist = new ArrayList<>();
        final ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
            .withQueueUrl(sqsUrl)
            .withWaitTimeSeconds(20);
        List<Message> messages = sqs.receiveMessage(receive_request).getMessages();

        // Delete messages from sqs
        for (Message m : messages) {
            sqs.deleteMessage(sqsUrl, m.getReceiptHandle());
        }

        for(Message m: messages)
        {
            messagelist.add(m.getBody());
        }

        return messagelist;
    }
}
