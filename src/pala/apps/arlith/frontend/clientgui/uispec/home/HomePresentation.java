package pala.apps.arlith.frontend.clientgui.uispec.home;

import pala.apps.arlith.frontend.clientgui.Presentation;

/**
 * The Home scene is the "main" scene for the client frontend. It shows up
 * immediately after log in and acts as a "hub" through which other UIs can be
 * accessed, either directly or indirectly. The Home scene is currently simple
 * in design. It should show users threads they have access to and allow users
 * to read and send messages in those threads.
 * 
 * @author Palanath
 *
 */
public interface HomePresentation extends Presentation<HomeLogic> {

}
