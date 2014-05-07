package com.ncr.ATMMonitoring.serverchain.message.specific.strategy.imp;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ncr.ATMMonitoring.routertable.RouterTableHandler;
import com.ncr.ATMMonitoring.serverchain.ChildrenLinkListHandler;
import com.ncr.ATMMonitoring.serverchain.message.specific.SpecificMessage;
import com.ncr.ATMMonitoring.serverchain.message.specific.incoming.UpdateRouterTable;
import com.ncr.ATMMonitoring.serverchain.message.specific.strategy.BroadcastType;
import com.ncr.ATMMonitoring.serverchain.message.wrapper.MessageWrapper;
import com.ncr.ATMMonitoring.serverchain.message.wrapper.OutgoingMessage;

/**
 * @author Otto Abreu
 * 
 */
public class UpdateRouterTableStrategy extends BaseStrategy {

    private UpdateRouterTable updateMessage;
    private static final Logger logger = Logger
	    .getLogger(UpdateRouterTableStrategy.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.message.specific.strategy.
     * SpecifcMessageProcessStrategy
     * #setupStrategy(com.ncr.ATMMonitoring.serverchain.NodeInformation,
     * com.ncr.ATMMonitoring.serverchain.message.specific.SpecificMessage)
     */
    @Override
    public void setupStrategy(SpecificMessage message,
	    ApplicationContext springContext) {

	super.setupStrategy(message, springContext);
	this.updateMessage = this.castMessageToUpdateRouterTable(message);

    }

    private UpdateRouterTable castMessageToUpdateRouterTable(
	    SpecificMessage message) {
	return ((UpdateRouterTable) message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.message.specific.strategy.
     * SpecifcMessageProcessStrategy#canProcessSpecificMessage()
     */
    @Override
    public boolean canProcessSpecificMessage() {

	if (matriculaIsPresentInLocalTable()
		&& diferentProcessingNodePressent()) {

	    logger.debug("is going to update he router table");
	    return true;

	} else if (!matriculaIsPresentInLocalTable() && isUpdate()) {

	    logger.debug("is going to add to the router table");
	    return true;
	}

	logger.debug("is NOT going to update the router table: "
		+ RouterTableHandler.tableTotring());
	return false;
    }

    private boolean matriculaIsPresentInLocalTable() {

	int matricula = this.updateMessage.getMatricula();
	boolean isPresent = RouterTableHandler
		.matriculaIsInRouterTable(matricula);

	return isPresent;

    }

    private boolean diferentProcessingNodePressent() {

	int matricula = this.updateMessage.getMatricula();
	String newProcessingNode = this.updateMessage.getNewFinalNodeInCharge();
	String actualNodeInCharge = RouterTableHandler
		.getNodeInCharge(matricula);

	if (newNodeIsDifferentFromActual(actualNodeInCharge, newProcessingNode)) {

	    return true;

	} else {

	    return false;
	}
    }

    private boolean newNodeIsDifferentFromActual(String actualNodeInCharge,
	    String newProcessingNode) {

	if (StringUtils.isNotEmpty(actualNodeInCharge)
		&& !actualNodeInCharge.equals(newProcessingNode)) {

	    return true;

	} else {

	    return false;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.message.specific.strategy.
     * SpecifcMessageProcessStrategy#processSpecificMessage()
     */
    @Override
    public void processSpecificMessage() {

	int matricula = this.updateMessage.getMatricula();
	this.processByNodePosition(matricula);

    }

    private void processByNodePosition(int matricula) {

	if (isRoot()) {
	    // i only change the type after arriving the root
	    // Because i add or update in my way up
	    this.updateMessage
		    .setUpdateType(UpdateRouterTable.UpdateType.REMOVE_ONLY);

	    logger.debug("is root so is going to change to REMOVE ONLY");

	} else if (isMiddleOrLeaf()) {
	    logger.debug("is middleorleaf so is going to update or add");
	    logger.debug("The table before: "
		    + RouterTableHandler.tableTotring());
	    this.proccessByOperationMiddle(matricula);
	    logger.debug("The updated table: "
		    + RouterTableHandler.tableTotring());

	}

    }

    private boolean isMiddleOrLeaf() {

	if (isMiddle() || isLeaf()) {
	    return true;

	} else {

	    return false;
	}
    }

    private void proccessByOperationMiddle(int matricula) {

	if (isUpdate() || isForcedUpdateFromRoot()) {

	    if (!isOriginalProcessingNode()
		    && !isOldProcessingNodeaAChild(matricula)) {

		this.updateValueInTable(matricula);

	    } else if (!isOriginalProcessingNode()
		    && isOldProcessingNodeaAChild(matricula)) {

		RouterTableHandler.removeMatriculaFromTable(matricula);

	    } else if (isOriginalProcessingNode()) {

		RouterTableHandler.removeMatriculaFromTable(matricula);

	    } else {

		this.addValueToTable(matricula);
	    }

	} else if (isRemoveOnly()) {

	    RouterTableHandler.removeMatriculaFromTable(matricula);
	}
    }

    private boolean isUpdate() {

	if (this.updateMessage.getUpdateType().equals(
		UpdateRouterTable.UpdateType.UPDATE)) {

	    return true;

	} else {

	    return false;
	}
    }

    private boolean isForcedUpdateFromRoot() {

	if (this.updateMessage.getUpdateType().equals(
		UpdateRouterTable.UpdateType.FORCE_UPDATE_FROM_ROOT)) {

	    return true;

	} else {

	    return false;
	}
    }

    private boolean isOriginalProcessingNode() {
	if (matriculaIsPresentInLocalTable()
		&& diferentProcessingNodePressent() && isLeaf()) {

	    return true;

	} else {

	    return false;
	}
    }

    private boolean isOldProcessingNodeaAChild(int matricula) {

	String actualNodeInCharge = RouterTableHandler
		.getNodeInCharge(matricula);
	ChildrenLinkListHandler childrenLinkListHandler = this
		.getChildrenLinkListHandler();
	return childrenLinkListHandler.isChildSubscribed(actualNodeInCharge);

    }

    private void updateValueInTable(int matricula) {

	RouterTableHandler.removeMatriculaFromTable(matricula);
	this.addValueToTable(matricula);
    }

    private void addValueToTable(int matricula) {
	String ipNodeInCharge = this.updateMessage.getNewFinalNodeInCharge();
	RouterTableHandler.addMatriculaAndIpToTable(matricula, ipNodeInCharge);
    }

    private boolean isRemoveOnly() {

	if (this.updateMessage.getUpdateType().equals(
		UpdateRouterTable.UpdateType.REMOVE_ONLY)) {
	    return true;
	} else {
	    return false;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.message.specific.strategy.
     * SpecifcMessageProcessStrategy#broadcastDirection()
     */
    @Override
    public BroadcastType broadcastDirection() {

	BroadcastType broadcast = BroadcastType.NONE;

	if (this.isRoot()) {

	    broadcast = BroadcastType.TURN_BACK;

	} else if (this.isMiddle() && this.isUpdate()) {

	    broadcast = BroadcastType.TWO_WAY;

	} else if (this.isMiddle() && this.isForcedUpdateFromRoot()) {

	    broadcast = BroadcastType.ONE_WAY;

	} else if (this.isMiddle() && this.isRemoveOnly()
		& this.canProcessSpecificMessage()) {

	    broadcast = BroadcastType.ONE_WAY;
	}

	return broadcast;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ncr.ATMMonitoring.serverchain.message.specific.strategy.
     * SpecifcMessageProcessStrategy#getTurnBackMessage()
     */
    @Override
    public MessageWrapper getTurnBackMessage() {

	OutgoingMessage outgoingMessage = new OutgoingMessage(
		"Updating table outgoing", this.updateMessage.getMatricula());
	outgoingMessage.setSpecificMessage(this.updateMessage);
	logger.debug("the command in turnback-->: "
		+ this.updateMessage.getUpdateType());
	return outgoingMessage;
    }

    private ChildrenLinkListHandler getChildrenLinkListHandler() {
	return (ChildrenLinkListHandler) this
		.getSpringBean(ChildrenLinkListHandler.class);
    }

}