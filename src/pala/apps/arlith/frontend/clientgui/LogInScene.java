package pala.apps.arlith.frontend.clientgui;

import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.libraries.frontends.FrontendScene;

public class LogInScene implements FrontendScene<ClientGUIFrontend> {
	private final ClientGUIFrontend frontend;
	private final ArlithClientBuilder builder;

	public LogInScene(ClientGUIFrontend frontend, ArlithClientBuilder builder) {
		this.frontend = frontend;
		this.builder = builder;
	}

	@Override
	public ClientGUIFrontend getFrontend() {
		return frontend;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

}
