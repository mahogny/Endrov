package endrov.imagesetOME.nytt;

import omero.ServerError;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

public class ConnectTest
	{
	
	public static void main(String[] args)
		{
		
		omero.client c = new omero.client();
		try
			{
			

			
			omero.api.ServiceFactoryPrx s = c.createSession();

			// IAdmin is responsible for all user/group creation, password changing, etc.
			omero.api.IAdminPrx admin  = s.getAdminService();

			// Who you are logged in as.
			System.out.println(admin.getEventContext().userName);

			// These two services are used for database access
			omero.api.IQueryPrx query = s.getQueryService();
			omero.api.IUpdatePrx update = s.getUpdateService();
			
			/*
			Login login = new Login("root", rootpass);
				        ServiceFactory factory = new ServiceFactory(login);
				        IQuery iQuery = factory.getQueryService();
				        iQuery.get(Experimenter.class, 0L);
			*/
			
			
			
			
			}
		catch (CannotCreateSessionException e)
			{
			e.printStackTrace();
			}
		catch (PermissionDeniedException e)
			{
			e.printStackTrace();
			}
		catch (ServerError e)
			{
			e.printStackTrace();
			}
		finally
			{
			c.closeSession();
			}
		
		}

	}
