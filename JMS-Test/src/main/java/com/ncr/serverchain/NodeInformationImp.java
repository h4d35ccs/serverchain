/**
 * 
 */
package com.ncr.serverchain;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * Concrete class of NodeInformation.
 * 
 * Returns the information regarding the node, such as the position, the topics
 * names, the brokers URL
 * 
 * @author Otto Abreu
 * 
 * </pre>
 * 
 */
@Component
public class NodeInformationImp implements NodeInformation {

    private static final String PROPERTY_FILE_NAME = "chainconfig.properties";

    private static final String PARENT_OUTGOING_TOPIC_URL_KEY = "jms.parent.outgoing.topic.url";
    private String parentOutgoingTopicUrl;

    private static final String OUTGOING_TOPIC_NAME_KEY = "jms.outgoing.topic.name";
    private String outgoingTopicName;

    private static final String LOCAL_BROKER_URL_KEY = "jms.localbroker.url";
    private String localBrokerUrl;

    private static final String INCOMING_TOPIC_NAME_KEY = "jms.incoming.topic.name";
    private String incomingTopicName;

    private static final String BROKER_URL_PATTERN_KEY = "jms.broker.url.pattern";
    private String brokerUrlPattern;

    private static final String PATTERN_IP_TOKEN = "\\{ip\\}";

    @Autowired
    private ChildrenLinkListHandler childrenLinkListHandler;

    @PostConstruct
    public void init() throws IOException {

	Properties nodeInfoProps = PropertiesLoaderUtils
		.loadProperties(new ClassPathResource(PROPERTY_FILE_NAME));
	this.setPrivateValuesFromProperties(nodeInfoProps);
    }

    private void setPrivateValuesFromProperties(Properties nodeInfoProps) {
	this.parentOutgoingTopicUrl = this.getPropertyValue(nodeInfoProps,
		PARENT_OUTGOING_TOPIC_URL_KEY);

	this.outgoingTopicName = this.getPropertyValue(nodeInfoProps,
		OUTGOING_TOPIC_NAME_KEY);
	this.localBrokerUrl = this.getPropertyValue(nodeInfoProps,
		LOCAL_BROKER_URL_KEY);
	this.incomingTopicName = this.getPropertyValue(nodeInfoProps,
		INCOMING_TOPIC_NAME_KEY);
	this.brokerUrlPattern = this.getPropertyValue(nodeInfoProps,
		BROKER_URL_PATTERN_KEY);
    }

    private String getPropertyValue(Properties nodeInfoProps, String key) {
	String propertyValue = nodeInfoProps.getProperty(key, "");
	return propertyValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#getNodePosition()
     */
    @Override
    public NodePosition getNodePosition() {
	
	NodePosition nodePosition = NodePosition.ONLY_NODE;

	if (this.isRoot()) {

	    nodePosition = NodePosition.FIRST_NODE;

	} else if (this.isLeaf()) {

	    nodePosition = NodePosition.LEAF_NODE;

	} else if (this.isMiddleNode()) {

	    nodePosition = NodePosition.MIDDLE_NODE;

	}

	return nodePosition;
    }
    
    public boolean isRoot(){
	if (this.isFirstNode() && !isOnlyNode()) {
	    return true;
	}else{
	    return false;
	}
    }

    private boolean isFirstNode() {
	boolean isFirstNode = false;

	if (!this.hasParentNode()) {
	    isFirstNode = true;
	}
	return isFirstNode;
    }

    public boolean isLeaf() {
	boolean isLeaf = false;

	if (hasParentNode() && (this.getChildrenSubscribed().isEmpty())) {
	    isLeaf = true;
	}

	return isLeaf;
    }

    public boolean isMiddleNode() {

	boolean isMiddleNode = false;

	if (hasParentNode() && (!this.getChildrenSubscribed().isEmpty())) {
	    isMiddleNode = true;
	}
	return isMiddleNode;
    }

    public boolean isOnlyNode() {

	boolean isOnlyNode = false;

	if (isFirstNode() && this.getChildrenSubscribed().isEmpty()) {

	    isOnlyNode = true;
	}
	return isOnlyNode;
    }

    private Set<String> getChildrenSubscribed() {
	Set<String> childrenSubscribed = null;
	childrenSubscribed = this.childrenLinkListHandler
		.getChildrenSubscribed();
	return childrenSubscribed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#hasParentNode()
     */
    @Override
    public boolean hasParentNode() {
	boolean hasParent = false;

	if (!StringUtils.isEmpty(this.parentOutgoingTopicUrl)) {

	    hasParent = true;
	}
	return hasParent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#
     * getParentOutgoingTopicUrl()
     */
    @Override
    public String getParentOutgoingTopicUrl() {
	String parentOutgoingTopicUrl = "";

	parentOutgoingTopicUrl = this
		.generateRemoteBrokerUrl(this.parentOutgoingTopicUrl);

	return parentOutgoingTopicUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#getOutgoingTopicName
     * ()
     */
    @Override
    public String getOutgoingTopicName() {
	return outgoingTopicName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#getLocalBrokerUrl
     * ()
     */
    @Override
    public String getLocalBrokerUrl() {
	return localBrokerUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#getIncomingTopicName
     * ()
     */
    @Override
    public String getIncomingTopicName() {

	return this.incomingTopicName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.ChainLinkInformation#
     * generateRemoteBrokerUrl(java.lang.String)
     */
    @Override
    public String generateRemoteBrokerUrl(String ip) {
	String completeRemoteUrl = "";

	if (!StringUtils.isEmpty(this.brokerUrlPattern)) {

	    completeRemoteUrl = this.brokerUrlPattern;
	    completeRemoteUrl = completeRemoteUrl.replaceFirst(
		    PATTERN_IP_TOKEN, ip);

	} else {

	    completeRemoteUrl = ip;
	}
	return completeRemoteUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.NodeInformation#getLocalUrl()
     */
    @Override
    public String getLocalUrl() {
	String localUrl = "";
	if (StringUtils.isNotEmpty(this.localBrokerUrl)) {
	    String[] protocolAndUrl = this.localBrokerUrl.split("://");
	    localUrl = protocolAndUrl[1];
	}
	return localUrl;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.NodeInformation#getParentUrl()
     */
    @Override
    public String getParentUrl() {
	return this.parentOutgoingTopicUrl;
    }

}
