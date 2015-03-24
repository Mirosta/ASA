package com.tw10g12.IO;

import org.json.JSONObject;

/**
 * Created by Tom on 16/03/2015.
 */
public interface JSONSerializable
{
    public JSONObject toJSON(JSONObject obj);
}
