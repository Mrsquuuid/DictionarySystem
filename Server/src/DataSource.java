import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

/**
 * @author Yuzhe You (No.1159774)
 * Dictionary Encapsulation.
 */

public class DataSource {
	private JSONObject dictionaryJsonObj;
	
	public DataSource(String dictionaryPath) throws IOException, ParseException{
		this.dictionaryJsonObj = new JSONObject();
		this.dictionaryJsonObj = loadDictionary(dictionaryPath);
	}

	public JSONObject getDictionaryJsonObj() {
		return dictionaryJsonObj;
	}

	public void setDictionaryJsonObj(JSONObject dictionaryJsonObj) {
		this.dictionaryJsonObj = dictionaryJsonObj;
	}

	public synchronized boolean alreadyInDictionary(String word) {
		return dictionaryJsonObj.get(word)!=null;
	}

	public synchronized JSONObject loadDictionary(String dataSourcePath)
			throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		try{
			File dictionary = new File(dataSourcePath);
			FileReader dictionaryReader = new FileReader(dictionary);
			JSONObject dictionaryJsonObj = (JSONObject) jsonParser.parse(dictionaryReader);
			//dictionaryJsonObj.writeJSONString();
			return dictionaryJsonObj;
		}catch(IOException e) {
			throw new IOException("Dictionary does not exist!");

		}catch(ParseException e) {
			throw new ParseException(0, "Incorrect dictionary format!");
		}
	}

	public void writeJsonFile(JSONObject jsonObject, String path){
		try {
			String newJsonString = jsonObject.toString();
			FileWriter fw = new FileWriter(path);
			PrintWriter out = new PrintWriter(fw);
			out.write(newJsonString);
			out.println();
			fw.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JSONObject queryEncapsulate(String word, String definition) {
		JSONObject result = new JSONObject();
		result.put("method", "query");
		result.put("word", word);
		if(alreadyInDictionary(word) && definition==null) {
			result.put("status", "success");
			result.put("feedback", dictionaryJsonObj.get(word) );
			return result;
		}else {
			if (!alreadyInDictionary(word) && definition != null) {
				result.put("status", "failure");
				result.put("feedback", "Word does not exist in dictionary!");
				return result;
			} else if (alreadyInDictionary(word) && definition != null) {
				result.put("status", "failure");
				result.put("feedback", "Word already exists in the dictionary!");
				return result;
			} else {
				result.put("status", "failure");
				result.put("feedback", "Word does not exist in the dictionary!");
				return result;
			}
		}
	}

	public synchronized JSONObject addEncapsulate(String word, String definition) {
		JSONObject result = new JSONObject();
		result.put("method", "add");
		result.put("word",word);
		if(alreadyInDictionary(word)) {
			result.put("status", "failure");
			result.put("feedback","Word already exists in the dictionary!" );
			return result;
		}
		if (!alreadyInDictionary(word) && definition != null){
			dictionaryJsonObj.put(word, definition);
			result.put("status", "success");
			result.put("feedback","Word has been added!" );
		}else{
			result.put("status", "failure");
			result.put("feedback","Word or definition should not be null!" );
		}
		return result;
	}

	public synchronized JSONObject removeEncapsulate(String word, String definition){
		JSONObject result = new JSONObject();
		result.put("method", "remove");
		result.put("word", word);
		if(alreadyInDictionary(word) && definition==null) {
			dictionaryJsonObj.remove(word);
			result.put("status", "success");
			result.put("feedback","Word has been removed!");
			return result;
		}
		result.put("status", "failure");
		if (alreadyInDictionary(word) && definition!=null) {
			result.put("feedback","Word already exists in the dictionary!");
		}else {
			result.put("feedback","Word does not exist!" );
		}
		return result;
	}


	public synchronized JSONObject updateEncapsulate(String word, String definition){
		JSONObject result = new JSONObject();
		result.put("method", "update");
		result.put("word",word);
		if(!alreadyInDictionary(word)) {
			result.put("status", "failure");
			result.put("feedback","Word does not exists in the dictionary!" );
			return result;
		}else if(alreadyInDictionary(word) && definition != null) {
			dictionaryJsonObj.remove(word);;
			dictionaryJsonObj.put(word, definition);
			result.put("status", "success");
			result.put("feedback","Word has been updated!" );
		}else{
			result.put("status", "failure");
			result.put("feedback","Word or definition should not be null!" );
		}
		return result;
	}

}
	
	
	

