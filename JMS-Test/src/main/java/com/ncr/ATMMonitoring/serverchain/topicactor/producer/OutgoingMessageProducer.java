package com.ncr.ATMMonitoring.serverchain.topicactor.producer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("outgoingProducer")
public class OutgoingMessageProducer  extends GenericMessageProducer  {

    @Autowired
    private ActiveMQConnectionFactory localConnectionFactory;

    @Override
    protected String getTopicName() {
	
	return this.getOutgoingTopicName();
    }

    @Override
    protected ActiveMQConnectionFactory getLocalConnectionFactory() {
	
	return this.localConnectionFactory;
    }

 
    
   

}
