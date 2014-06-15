package i5.las2peer.services.servicePackage;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.UserAgent;


/**
 * 
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * @author Peter de Lange
 *
 */
@Path("example")
@Version("0.1")
public class ServiceClass extends Service {
	
	
	/**
	 * This method is needed for every RESTful application in LAS2peer.
	 * 
	 * @return the mapping
	 */
    public String getRESTMapping()
    {
        String result="";
        try {
            result= RESTMapper.getMethodsAsXML(this.getClass());
        } catch (Exception e) {

            e.printStackTrace();
        }
        return result;
    }
    
    
    /**
     * 
     * Simple function to validate a user login.
     * Basically it only serves as a "calling point" and does not really validate a user
     *(since this is done previously by LAS2peer itself, the user does not reach this method
     * if he or she is not authenticated).
     * 
     */
    @GET
    @Path("validate")
    public String validateLogin()
    {
    	String returnString = "";
    	returnString += "You are " + ((UserAgent) getActiveAgent()).getLoginName() + " and you're login is valid!";
    	return returnString;
    }
    
    /**
     * 
     * Another example method.
     * 
     * @param myInput
     * 
     */
    @POST
    @Path("myMethodPath/{input}")
    public String exampleMethod( @PathParam("input") String myInput)
    {
    	String returnString = "";
    	returnString += "You have entered " + myInput + "!";
    	return returnString;
    }
    
}
