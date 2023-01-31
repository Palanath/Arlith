package pala.apps.arlith.frontend.clientgui;

import pala.apps.arlith.libraries.frontends.Frontend;

/**
 * <p>
 * A class representing a specific {@link Theme} for some interfaces. The theme
 * is responsible for instantiating and providing the supervising
 * {@link Frontend} with presentation implementations for the {@link Logic}s
 * that the theme supports.
 * </p>
 * <p>
 * Typically the class-hierarchy&ndash;layout involving a {@link Logic} is that
 * a subclass of <code style="color: purple;">UserInterface</code> specifies a
 * corresponding <code style="color: purple;">Presentation</code>. The
 * application will, at some point, instantiate that user
 * interface&ndash;subclass and then need a presentation to expose it to the
 * user. The application will then query whatever active {@link Theme} instance
 * is set up, expecting to receive a presentation to supply the {@link Logic}.
 * </p>
 * <p>
 * For most GUIs, the {@link Logic} needs to have access to its accompanying,
 * active presentation, and vice versa, although, this is not enforced by the
 * API classes.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Theme {
	/**
	 * Instantiates and returns an object implementing the specified {@link Logic}'s
	 * corresponding presentation <code>interface</code>, or <code>null</code> if
	 * this theme does not have a presentation for the specified interface.
	 * 
	 * @param <P>           The type of the presentation <code>interface</code>
	 *                      corresponding to the {@link Logic} supplied. The
	 *                      {@link Logic} relies on an instance of <code>P</code> to
	 *                      present the interface to the end user. This method is
	 *                      expected to supply this theme's instance of
	 *                      <code>P</code>.
	 * @param userInterface
	 * @return
	 */
	<P extends Presentation<L>, L extends Logic<P>> P supply(L userInterface);
}
