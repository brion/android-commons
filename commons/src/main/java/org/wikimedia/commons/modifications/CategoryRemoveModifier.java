package org.wikimedia.commons.modifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        return pageContents;
    }

    @Override
    public String getEditSumary() {
        return String.format("Removed " + params.optJSONArray(PARAM_CATEGORYREMOVE).length() + " categories.");
    }
}
