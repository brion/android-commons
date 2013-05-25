package org.wikimedia.commons.modifications;

import android.util.Log;
import android.util.Xml;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wikimedia.commons.Utils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryRemoveModifier extends PageModifier {


    public static String PARAM_CATEGORYREMOVE = "categoryremove";

    public static String MODIFIER_NAME = "CategoryRemoveModifier";

    public CategoryRemoveModifier(String... categories) {
        super(MODIFIER_NAME);
        JSONArray categoriesArray = new JSONArray();
        for(String category: categories) {
            categoriesArray.put(category);
        }
        try {
            params.putOpt(PARAM_CATEGORYREMOVE, categoriesArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public CategoryRemoveModifier(JSONObject data) {
        super(MODIFIER_NAME);
        this.params = data;
    }

    @Override
    public String doModification(String pageName, String pageContents, String pageXml) {
        // Hacky regexes. Awesome.
        Pattern pattern = Pattern.compile("\\[\\[\\s*Category\\s*:\\s*([^]]+)(\\|[^]]*)?\\]\\]");
        Matcher matches = pattern.matcher(pageContents);
        while (matches.find()) {
            String category = Utils.capitalize(matches.group(1));
            Log.d("Commons", "page has category: " + category);
        }
        return pageContents;
    }

    @Override
    public String getEditSumary() {
        return String.format("Removed " + params.optJSONArray(PARAM_CATEGORYREMOVE).length() + " categories.");
    }
}
