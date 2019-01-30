/**
 * Created by charbonn on 15.11.2018.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyException;
import java.util.HashMap;
import java.util.Map;

public class ParamSet
{

	Map<String, String> params;

	public ParamSet(JSONArray a) throws KeyException
	{
		params = new HashMap<String, String>();
		for (int i = 0; i < a.length(); i++) {
			JSONObject param = a.getJSONObject(i);
			String att = param.getString("name");
			String val = param.getString("value");
			if (params.containsKey(att))
				throw new KeyException("Trying do define the same parameter more than once.");
			params.put(att, val);
		}
	}

	public Map<String, String> getParams()
	{
		return params;
	}

	public String get(String key)
	{
		return params.get(key);
	}
}
