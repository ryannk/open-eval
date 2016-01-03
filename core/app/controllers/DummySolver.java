package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.libs.ws.*;
import play.libs.F.Promise;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
	WSRequest info;
	WSRequest instance;
	
	/**
	 * Constructor. Initiates WSRequest object to send Http requests
	 * @param url - Url of the server to send instances to
	 */
	public DummySolver(String url) {
		this.info = WS.url(url+"/info");
		this.instance = WS.url(url+"/instance");
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotation - The unsolved instance to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public WSResponse processRequest(TextAnnotation textAnnotation) {
		String taJson = SerializationHelper.serializeToJson(textAnnotation);
		Promise<WSResponse> jsonPromise = instance.post(taJson);

		return jsonPromise.get(5000);
	}
	
	public String getInfo(){
		String jsonInfo;
		try{
			Promise<WSResponse> responsePromise = info.get();
			WSResponse response = responsePromise.get(5000);
			jsonInfo = response.getBody();
		}	
		catch(Exception e){
			jsonInfo = "err";
		}
		return jsonInfo;
	}
}