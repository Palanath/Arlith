package pala.apps.arlith.backend.client.api;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.types.MessageValue;

public class ClientMessage extends SimpleClientObject {
	private String text;
	private ClientUser author;
	private ClientThread thread;

	// As of right now, ClientMessages are only ever provided to a Client,
	// from a Server, in a way that includes all the values provided by
	// this parameter. For this reason, this class need not do "late-loading"
	// operations by supporting having instances be built off of simply a GID, and
	// then lazily loading values of properties as they are requested.
	//
	// For more information, the server is designed not to let clients obtain a
	// Message by GID because it does not map GIDs of Messages to messages. This
	// could grow to be an unnecessariy large map, especially for my measly laptop.

	// The various constructors that a ClientObject class may have represent the
	// different ways that a client may receive such an object. In our
	// case here, as it is, information about a ClientMessage may only be obtained
	// when
	// the client learns about the ClientMessage via a request or event. With
	// ClientThreads and ClientUsers, a client may learn about a thread or user via
	// a reference (i.e.a request of some sort returns a GID to a newly created
	// thread, but no other information about the thread). In such a scenario,
	// respective classes (in our example, the ClientThread class) will provide
	// constructors that only require a GID and a Client (the latter, of course, is
	// for internal stuff). When subsequent properties about the thread are required
	// by whatever uses this Application Client API, this API will request those
	// properties in the getters of the ClientThread class, and will update itself
	// to store those properties accordingly. Some ClientObject classes may even
	// provide a constructor that takes only a GID and a constructor that takes
	// some, or all of the object's properties, and a GID. This is because there are
	// means of obtaining one of those objects from Arlith servers where more than
	// just the GID is known at the time of obtaining. Any unknown properties can
	// also be requested.

	// It is basically a given that if a ClientObject class has a constructor that
	// does not fill all of its properties up, there must be some way to request
	// such properties from Application Servers.

	public ClientMessage(GID gid, String content, GID author, GID thread, ArlithClient client) {
		super(gid, client);
		text = content;
		this.author = client.getUser(author);
		this.thread = client.getThread(thread);
	}

//	public ClientMessage(GID gid, Client client) {
//		super(gid, client);
//	}

//	private <R> RequestAction<GetMessageRequest, MessageValue, R> request() {
//
//	}

	public ClientMessage(MessageValue msg, ArlithClient client) {
		this(msg.id(), msg.content(), msg.getSenderUser().getGid(), msg.getOwnerThread().getGid(), client);
	}

	public ClientUser getAuthor() {
		return author;
	}

	public String getText() {
		return text;
	}

	public String text() {
		return getText();
	}

	public ClientThread getThread() {
		return thread;
	}

	@Override
	public String toString() {
		return getText();
	}

}
