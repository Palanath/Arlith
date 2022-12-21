package pala.apps.arlith.frontend.guis.communityview;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.ClientCommunity;
import pala.apps.arlith.frontend.guis.BindHandlerPage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class CommunityViewPage extends BindHandlerPage {

	private final ClientCommunity community;
	private ArlithClient client;

	public CommunityViewPage(ClientCommunity community) {
		this.community = community;
	}

	@Override
	public void show(ArlithWindow window) throws WindowLoadFailureException {
		client = window.getApplication().getClient();
	}

}
