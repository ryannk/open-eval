import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class InstanceController implements RouterNanoHTTPD.UriResponder
{
    @Override
    public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession session)
    {
        Annotator annotator = uriResource.initParameter(Annotator.class);
        JsonArray jInstances;
        JsonParser parser = new JsonParser();

        try{
            String body = readBody(session);
            jInstances = parser.parse(body).getAsJsonArray();
        } catch (Exception ex){
            return ResponseGenerator.generateResponse(String.format("Error reading request: %s", ex.toString()), Response.Status.BAD_REQUEST);
        }

        TextAnnotation[] textAnnotations = new TextAnnotation[jInstances.size()];
        String[] errors = new String[textAnnotations.length];

        parseTextAnnotations(jInstances, textAnnotations, errors);
        long[] runTimes = annotateInstances(annotator, textAnnotations, errors);

        JsonArray newJInstances = serializeInstances(parser, textAnnotations, runTimes, errors);

        return NanoHTTPD.newFixedLengthResponse(newJInstances.toString());
    }

    private JsonArray serializeInstances(JsonParser parser, TextAnnotation[] textAnnotations, long[] runTimes, String[] errors) {
        JsonArray newJInstances = new JsonArray();
        for(int i=0;i<textAnnotations.length;i++){
            JsonObject instanceObject = new JsonObject();

            if(textAnnotations[i] != null) {
                String jsonTextAnnotation = SerializationHelper.serializeToJson(textAnnotations[i]);
                JsonObject jTextAnnotation = parser.parse(jsonTextAnnotation).getAsJsonObject();
                instanceObject.add("textAnnotation", jTextAnnotation);
                instanceObject.add("runTime", new JsonPrimitive(runTimes[i]));
            }
            if(errors[i] != null){
                instanceObject.add("error", new JsonPrimitive(errors[i]));
            }
            newJInstances.add(instanceObject);
        }
        return newJInstances;
    }

    private long[] annotateInstances(Annotator annotator, TextAnnotation[] textAnnotations, String[] errors) {
        long[] runTimes = new long[textAnnotations.length];
        for(int i=0;i<textAnnotations.length;i++) {
            if (textAnnotations[i] != null){
                try{
                    long startTime = System.currentTimeMillis();
                    annotator.addView(textAnnotations[i]);
                    long endTime = System.currentTimeMillis();
                    runTimes[i] = endTime - startTime;
                } catch (AnnotatorException e) {
                    textAnnotations[i] = null;
                    errors[i] = String.format("There was an error adding the view to the instance: %s", e.toString());
                }
            }
        }
        return runTimes;
    }

    private void parseTextAnnotations(JsonArray jInstances, TextAnnotation[] textAnnotations, String[] errors) {
        for(int i=0;i<textAnnotations.length;i++){
            try {
                textAnnotations[i] = SerializationHelper.deserializeFromJson(jInstances.get(i).toString());
            } catch (Exception e){
                textAnnotations[i] = null;
                errors[i] = String.format("There was an error parsing the TextAnnotation: %s", e.toString());
            }
        }
    }

    private String readBody(NanoHTTPD.IHTTPSession session) throws Exception{
        Map<String, String> files = new HashMap<String, String>();
        String body;
        session.parseBody(files);
        body = files.get("postData");
        return body;
    }

    @Override
    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }
}
