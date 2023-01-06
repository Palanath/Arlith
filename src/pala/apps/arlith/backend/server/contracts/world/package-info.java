/**
 * <h1>Server World API</h1>
 * <p>
 * This package houses the specification of the types making up the Server World
 * API. These classes are used to store and manage the server's objects.
 * </p>
 * <h2>Overview</h2>
 * <p>
 * The server represents (conceptual) Arlith objects through (programmatic) Java
 * objects; e.g., Communities, Threads, and Users all have programmatic
 * counterparts
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerCommunity},
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerThread}, and
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerUser},
 * respectively.
 * </p>
 * <p>
 * The programmatic version of these objects is specified by the interfaces in
 * this package which define what these types can do and how they should behave.
 * These Arlith objects are said to comprise the <i>Server World</i> and
 * interfaces specifying their programmatic counterparts comprise the Server
 * World API.
 * </p>
 * <p>
 * The Server World API specifies the functions that are needed by and available
 * to the {@link pala.apps.arlith.backend.server.ArlithServer} and its
 * constituent components (e.g. the
 * {@link pala.apps.arlith.backend.server.systems.AuthenticationSystem}, the
 * {@link pala.apps.arlith.backend.server.contracts.serversystems.RequestSystem},
 * and the {@link pala.apps.arlith.backend.server.systems.EventSystem}). The
 * server is able to function with <i>any</i> valid implementation of this API.
 * </p>
 * <h2>Structure</h2>
 * <p>
 * The Server World API centers around a
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerWorld}, which
 * keeps track, either directly or indirectly, of all other server objects
 * tracked by the program. Resultingly, the
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerWorld} has a few
 * methods for creating new
 * {@link pala.apps.arlith.backend.server.contracts.world.ServerUser}s,
 * obtaining {@link pala.apps.arlith.backend.server.contracts.world.ServerUser}s
 * by ID, checking if usernames, emails, phone numbers, or the like are already
 * allocated to an existing account, checking for or allocating new
 * discriminators, and more.
 * </p>
 * <p>
 * The {@link pala.apps.arlith.backend.server.contracts.world.ServerWorld} is
 * usually an <i>entrypoint</i> for code outside the API to access the API,
 * unless outside-code is given an object already (e.g.
 * {@link pala.apps.arlith.backend.server.contracts.serversystems.RequestHandler}s
 * can access the
 * {@link pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection}
 * which made the request, which, for most requests, will have an attached
 * {@link pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection#getUser()
 * user}).
 * </p>
 * <h2>Implementation</h2>
 * <p>
 * The API specifies a lot of requirements for the behavior of most methods, but
 * also leaves much up to implementations (e.g. storing objects while the server
 * is not running).
 * </p>
 * 
 * @author Palanath
 */

package pala.apps.arlith.backend.server.contracts.world;