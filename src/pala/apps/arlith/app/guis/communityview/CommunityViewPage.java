package pala.apps.arlith.app.guis.communityview;

import pala.apps.arlith.app.client.Client;
import pala.apps.arlith.app.client.api.ClientCommunity;
import pala.apps.arlith.app.guis.BindHandlerPage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class CommunityViewPage extends BindHandlerPage {

	private final ClientCommunity community;
	private Client client;

	public CommunityViewPage(ClientCommunity community) {
		this.community = community;
	}

	@Override
	public void show(ArlithWindow window) throws WindowLoadFailureException {
		client = window.getApplication().getClient();
	}

}
