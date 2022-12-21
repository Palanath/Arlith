package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.LongValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.backend.server.contracts.world.ServerMessage;
import pala.apps.arlith.backend.server.contracts.world.ServerThread;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;

public final class RequestHandlerUtils {
	private RequestHandlerUtils() {
	}

	public static UserValue fromUser(ServerUser user) {
	//		return new UserValue(new TextValue(user.getUsername()), new TextValue(user.getStatus()),
	//				new LongValue(user.getMessageCount()), new LongValue(user.getDiscriminator()), new GIDValue(user.getGID()));
			return new UserValue(new TextValue(user.getUsername()), new TextValue(""), new LongValue(0),
					new TextValue(user.getDiscriminator()), new GIDValue(user.getGID()));
		}

	public static ThreadValue fromThread(ServerThread thread) {
		return new ThreadValue(new TextValue((String) null), new GIDValue(thread.getGID()));
	}

	public static MessageValue fromMessage(ServerMessage message) {
		return new MessageValue(new TextValue(message.getContent()), new GIDValue(message.getAuthor().getGID()),
				new GIDValue(message.getThread().getGID()), new GIDValue(message.getGID()));
	}
}
