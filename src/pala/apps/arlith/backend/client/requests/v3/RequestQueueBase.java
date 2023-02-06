package pala.apps.arlith.backend.client.requests.v3;

import java.util.function.Consumer;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;

public abstract class RequestQueueBase extends RequestSerializerBase implements RequestQueue {
	@Override
	public <R> void queue(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler,
			Consumer<? super Throwable> errorHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized <R> R inquire(Inquiry<? extends R> inquiry) throws CommunicationProtocolError {
		// TODO Auto-generated method stub
		return super.inquire(inquiry);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
	}

}
