package pala.apps.arlith.app.server.systems;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.app.server.MalformedIncomingRequestException;
import pala.apps.arlith.app.server.RequestNotSupportedException;
import pala.apps.arlith.app.server.Server;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.app.server.contracts.serversystems.RequestSystem;
import pala.apps.arlith.app.server.reqhandlers.AuthRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.ChangeEmailRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.ChangePhoneNumberRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.ChangeUsernameRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.CreateAccountRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.CreateCommunityRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.FriendByGIDRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.FriendByNameRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetBunchOUsersRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetCommunityImageRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetCommunityOwnerRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetCommunityUsersRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetEmailRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetIncomingFriendRequestsRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetOutgoingFriendRequestsRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetOwnUserRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetPhoneNumberRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetProfileIconRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetStatusRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetThreadRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.GetUserRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.ListFriendsRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.ListJoinedCommunitiesRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.LoginRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.OpenDirectConversationRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.RetrieveMessagesBeforeRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.RetrieveMessagesRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.SendMessageRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.SetCommunityImageRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.SetProfileIconRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.SetStatusRequestHandler;
import pala.apps.arlith.app.server.reqhandlers.UnfriendRequestHandler;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.errors.ServerError;
import pala.apps.arlith.backend.communication.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.communication.protocol.requests.ChangeEmailRequest;
import pala.apps.arlith.backend.communication.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.backend.communication.protocol.requests.ChangeUsernameRequest;
import pala.apps.arlith.backend.communication.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.backend.communication.protocol.requests.CreateCommunityRequest;
import pala.apps.arlith.backend.communication.protocol.requests.FriendByGIDRequest;
import pala.apps.arlith.backend.communication.protocol.requests.FriendByNameRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetBunchOUsersRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetCommunityImageRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetCommunityOwnerRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetCommunityUsersRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetEmailRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetIncomingFriendRequestsRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetOutgoingFriendRequestsRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetOwnUserRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetPhoneNumberRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetProfileIconRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetStatusRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetThreadRequest;
import pala.apps.arlith.backend.communication.protocol.requests.GetUserRequest;
import pala.apps.arlith.backend.communication.protocol.requests.ListFriendsRequest;
import pala.apps.arlith.backend.communication.protocol.requests.ListJoinedCommunitiesRequest;
import pala.apps.arlith.backend.communication.protocol.requests.LoginRequest;
import pala.apps.arlith.backend.communication.protocol.requests.OpenDirectConversationRequest;
import pala.apps.arlith.backend.communication.protocol.requests.RetrieveMessagesBeforeRequest;
import pala.apps.arlith.backend.communication.protocol.requests.RetrieveMessagesRequest;
import pala.apps.arlith.backend.communication.protocol.requests.SendMessageRequest;
import pala.apps.arlith.backend.communication.protocol.requests.SetCommunityImageRequest;
import pala.apps.arlith.backend.communication.protocol.requests.SetProfileIconRequest;
import pala.apps.arlith.backend.communication.protocol.requests.SetStatusRequest;
import pala.apps.arlith.backend.communication.protocol.requests.UnfriendRequest;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.Connection;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;

public class RequestSystemImpl implements RequestSystem {

	private final Server server;
	private final Map<GID, List<RequestConnection>> requestClients = new HashMap<>();
	private final Map<String, RequestHandler> authenticatedRequestHandlers = new HashMap<>();
	
	{
		addHandler(ChangeUsernameRequest.REQUEST_NAME, new ChangeUsernameRequestHandler());
		addHandler(ChangeEmailRequest.REQUEST_NAME, new ChangeEmailRequestHandler());
		addHandler(ChangePhoneNumberRequest.REQUEST_NAME, new ChangePhoneNumberRequestHandler());
		addHandler(GetEmailRequest.REQUEST_NAME, new GetEmailRequestHandler());
		addHandler(GetPhoneNumberRequest.REQUEST_NAME, new GetPhoneNumberRequestHandler());
		addHandler(CreateCommunityRequest.REQUEST_NAME, new CreateCommunityRequestHandler());
		addHandler(SetCommunityImageRequest.REQUEST_NAME, new SetCommunityImageRequestHandler());
		addHandler(GetCommunityImageRequest.REQUEST_NAME, new GetCommunityImageRequestHandler());
		addHandler(GetProfileIconRequest.REQUEST_NAME, new GetProfileIconRequestHandler());
		addHandler(SetProfileIconRequest.REQUEST_NAME, new SetProfileIconRequestHandler());
		addHandler(CreateAccountRequest.REQUEST_NAME, new CreateAccountRequestHandler());
		addHandler(LoginRequest.REQUEST_NAME, new LoginRequestHandler());
		addHandler(AuthRequest.REQUEST_NAME, new AuthRequestHandler());
		addHandler(FriendByGIDRequest.REQUEST_NAME, new FriendByGIDRequestHandler());
		addHandler(FriendByNameRequest.REQUEST_NAME, new FriendByNameRequestHandler());
		addHandler(UnfriendRequest.REQUEST_NAME, new UnfriendRequestHandler());
		addHandler(GetOutgoingFriendRequestsRequest.REQUEST_NAME, new GetOutgoingFriendRequestsRequestHandler());
		addHandler(GetIncomingFriendRequestsRequest.REQUEST_NAME, new GetIncomingFriendRequestsRequestHandler());
		addHandler(GetUserRequest.REQUEST_NAME, new GetUserRequestHandler());
		addHandler(OpenDirectConversationRequest.REQUEST_NAME, new OpenDirectConversationRequestHandler());
		addHandler(RetrieveMessagesRequest.REQUEST_NAME, new RetrieveMessagesRequestHandler());
		addHandler(RetrieveMessagesBeforeRequest.REQUEST_NAME, new RetrieveMessagesBeforeRequestHandler());
		addHandler(SendMessageRequest.REQUEST_NAME, new SendMessageRequestHandler());
		addHandler(GetOwnUserRequest.REQUEST_NAME, new GetOwnUserRequestHandler());
		addHandler(ListFriendsRequest.REQUEST_NAME, new ListFriendsRequestHandler());
		addHandler(GetThreadRequest.REQUEST_NAME, new GetThreadRequestHandler());
		addHandler(SetStatusRequest.REQUEST_NAME, new SetStatusRequestHandler());
		addHandler(GetStatusRequest.REQUEST_NAME, new GetStatusRequestHandler());
		addHandler(GetCommunityOwnerRequest.REQUEST_NAME, new GetCommunityOwnerRequestHandler());
		addHandler(ListJoinedCommunitiesRequest.REQUEST_NAME, new ListJoinedCommunitiesRequestHandler());
		addHandler(GetCommunityUsersRequest.REQUEST_NAME, new GetCommunityUsersRequestHandler());
		addHandler(GetBunchOUsersRequest.REQUEST_NAME, new GetBunchOUsersRequestHandler());
	}

	public RequestSystemImpl(Server server) {
		this.server = server;
	}

	@Override
	public Server getServer() {
		return server;
	}

	@Override
	public void registerAuthenticatedRequestClient(RequestConnection connection) {
		JavaTools.putIntoListMap(requestClients, connection.getUserID(), connection, null);
	}

	@Override
	public List<RequestConnection> getAuthenticatedRequestClients(GID userID) {
		return requestClients.containsKey(userID) ? Collections.unmodifiableList(requestClients.get(userID))
				: Collections.emptyList();
	}

	@Override
	public Collection<RequestHandler> getRequestHandlers() {
		return authenticatedRequestHandlers.values();
	}

	@Override
	public void handleRequest(RequestConnection connection) throws UnknownCommStateException, BlockException,
			ClassCastException, MalformedIncomingRequestException, RequestNotSupportedException {
		// Read JSON object request.
		JSONObject request = (JSONObject) connection.getConnection().readJSON();
		// Make sure request contains the right header.
		if (!(request.get("request") instanceof JSONString))
			throw new MalformedIncomingRequestException(request);
		// Get the type of request being invoked.
		String req = request.getString("request");
		// If there is no handler registered in this manager for that type of request,
		// throw an exception.
		if (!authenticatedRequestHandlers.containsKey(req))
			throw new RequestNotSupportedException(request);
		// Invoke the appropriate handler.
		try {
			authenticatedRequestHandlers.get(req).handle(request, connection);
		} catch (Exception e) {
			connection.sendError(new ServerError());
			Logging.err(e);
			connection.closeConnection();// Log Connection out.
			unregisterRequestClient(connection);
		}
	}

	public class RequestConnectionImpl implements RequestConnection {
		private final Connection connection;
		private boolean active = true;

		public RequestConnectionImpl(Connection connection) {
			this.connection = connection;
		}

		private GID userID;

		@Override
		public Connection getConnection() {
			return connection;
		}

		@Override
		public boolean isAuthorized() {
			return userID != null;
		}

		@Override
		public GID getUserID() {
			return userID;
		}

		@Override
		public RequestSystem getManager() {
			return RequestSystemImpl.this;
		}

		@Override
		public void stopListening() {
			active = false;
		}

		@Override
		public boolean active() {
			return active;
		}

		@Override
		public void authorize(GID userID) {
			this.userID = userID;
		}
	}

	@Override
	public void unregisterRequestClient(RequestConnection connection) {
		JavaTools.removeFromListMap(requestClients, connection.getUserID(), connection);
	}

	@Override
	public void addHandler(String requestName, RequestHandler handler) {
		authenticatedRequestHandlers.put(requestName, handler);
	}

	@Override
	public void removeHandler(String requestName) {
		authenticatedRequestHandlers.remove(requestName);
	}
}
