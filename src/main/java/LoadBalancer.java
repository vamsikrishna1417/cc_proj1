
public class LoadBalancer{
	SqsHandler sqsHandler = new SqsHandler("inputMessageQueue");
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
					if(messagesToBeServed >= maxLimitInstances){
						count = ec2Server.startInstances(maxLimitInstances, count);
					}else{
						count = ec2Server.startInstances(messagesToBeServed, count);
					}
					count++;
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