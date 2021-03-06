/**
 * 
 */
package com.ncr.serverchain.message.wrapper;

import com.ncr.serverchain.message.wrapper.visitor.WrapperVisitor;


/**
 * <pre>
 * Define the wrapper used to Incoming messages.
 * 
 * The Incoming message is a message that starts in a leaf node and ends in a root
 * @author Otto Abreu
 * 
 * </pre>
 *
 */
public class IncomingMessage extends MessageWrapper {

 
    private static final long serialVersionUID = -6457607990812054891L;
    

    public IncomingMessage(String message, Long id) {
	super(message, id);

    }
    
    public IncomingMessage(Long id) {
   	super(DEFAULT_INCOMINGMESSAGE_INNER_MESSAGE, id);

       }

    @Override
    public void accept(WrapperVisitor visitor) {
	visitor.visit(this);

    }
}
