package org.wikimedia.commons.modifications;

import android.util.Xml;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

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
        // todo: parse pageXml

        InputStream in = null;
        try {
            in = new ByteArrayInputStream(pageXml.getBytes("UTF-8"));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // :P lazy
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
        }
        return pageContents;
    }

    @Override
    public String getEditSumary() {
        return String.format("Removed " + params.optJSONArray(PARAM_CATEGORYREMOVE).length() + " categories.");
    }
}
