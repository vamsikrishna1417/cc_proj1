import com.amazonaws.services.sqs.model.Message;

public class LoadBalancer{
	SqsHandler sqsHandler = new SqsHandler("inputMessageQueue");
	SqsHandler ec2InstanceHandler = new SqsHandler("instance.fifo");
	EC2Server ec2Server;
	public void scaleInAndOut(){
		int count=0;

		while(true){
			int messageCount = sqsHandler.getApproximateMessageCount();
			int ec2ClientInstances = ec2Server.getNumberOfInstances()-1; //bcoz master is counted as an instance
			if(messageCount>0 && messageCount>ec2ClientInstances){
				int maxLimitInstances = 19 - ec2ClientInstances;
				if(maxLimitInstances>0){
					int messagesToBeServed = messageCount - ec2ClientInstances;
					int countOfAvailableInstances = ec2InstanceHandler.getApproximateMessageCount();
					if(messagesToBeServed >= maxLimitInstances){
						while(countOfAvailableInstances != 0){
							Message instanceId = ec2InstanceHandler.ReceiveMessage();
							ec2InstanceHandler.deleteMessage(instanceId);
							if(instanceId != null){
								ec2Server.startInstance(instanceId.getBody());
								countOfAvailableInstances--;
							}else{
								break; // No available instances.
							}
						}
					}else{
						if(messagesToBeServed >= countOfAvailableInstances){
							while(countOfAvailableInstances != 0){
								Message instanceId = ec2InstanceHandler.ReceiveMessage();
								ec2InstanceHandler.deleteMessage(instanceId);
								if(instanceId != null){
									ec2Server.startInstance(instanceId.getBody());
									countOfAvailableInstances--;
								}else{
									break; // No available instances
								}
							}
						}else{
							while(messagesToBeServed != 0){
								Message instanceId = ec2InstanceHandler.ReceiveMessage();
								ec2InstanceHandler.deleteMessage(instanceId);
								ec2Server.startInstance(instanceId.getBody());
								messagesToBeServed--;
							}
						}
					}
				}
			}
			try{
				Thread.sleep(3000);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}