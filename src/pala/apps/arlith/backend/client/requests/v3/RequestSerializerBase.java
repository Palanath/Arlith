package pala.apps.arlith.backend.client.requests.v3;

import pala.apps.arlith.backend.client.ClientNetworkingBase;
import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v2.ConnectionStartupException;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public abstract class RequestSerializerBase extends ClientNetworkingBase implements RequestSerializer {

	@Override
	public synchronized <R> R inquire(Inquiry<? extends R> inquiry) throws IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, CommunicationProtocolError {
		if (!isRunning())
			throw new IllegalStateException("Request Serializer is shut down and cannot perform requests.");
		while (true)
			try {
				return inquiry.inquire(getConnection());
			} catch (UnknownCommStateException | BlockException e) {
				try {
					restartConnection();
				} catch (InterruptedException e1) {
					throw new IllegalStateException("Could not execute inquiry, (" + inquiry
							+ "), because Request Serializer is shut(ting) down.");
				}
			}
	}

}
