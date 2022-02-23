package news.androidtv.launchonboot;

/**
 * Created by guest1 on 1/2/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

/**
 * Version 1.1
 * Created by N on 14/9/2014.
 * Last Edited 13/5/2015
 *   * Support for syncing data to wearables
 */
public class SettingsManager {
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    protected String TAG = "PreferenceManager";
    protected Context mContext;
    public SettingsManager(Activity activity) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        mContext = activity;
        editor = sharedPreferences.edit();
    }
    public SettingsManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
        editor = sharedPreferences.edit();
    }
    public Context getContext() {
        return mContext;
    }
    public String getString(int resId) {
        return getString(mContext.getString(resId));
    }
    public String getString(String key) {
        return getString(key, "-1", "");
    }
    public String getString(int resId, String def) {
        return getString(mContext.getString(resId), def);
    }
    public String getString(String key, String def) {
        return getString(key, "-1", def);
    }
    public String getString(String key, String val, String def) {
        String result = sharedPreferences.getString(key, val);
        assert result != null;
        if(result.equals("-1")) {
            editor.putString(key, def);
            Log.d(TAG, key + ", " + def);
            editor.commit();
            result = def;
        }
        return result;
    }
    public String setString(int resId, String val) {
        return setString(mContext.getString(resId), val);
    }
    public String setString(String key, String val) {
        editor.putString(key, val);
        editor.commit();
        return val;
    }
    public boolean getBoolean(int resId) {
        return getBoolean(mContext.getString(resId));
    }
    public boolean getBoolean(int resId, boolean def) { return getBoolean(mContext.getString(resId), def);}
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    public boolean getBoolean(String key, boolean def) {
        boolean result;
        try {
            result = sharedPreferences.getBoolean(key, def);
        } catch(ClassCastException e) {
            //Result is not a boolean
            result = sharedPreferences.getString(key, def+"").equals("true");
            Log.d(TAG, "Recasted "+key+" with "+result);
        }
        editor.putBoolean(key, result);
        editor.commit();
        return result;
    }
    public boolean setBoolean(int resId, boolean val) {
        return setBoolean(mContext.getString(resId), val);
    }
    public boolean setBoolean(String key, boolean val) {
        editor.putBoolean(key, val);
        editor.commit();
        return val;
    }

    public int getInt(int resId) {
        try {
            return sharedPreferences.getInt(mContext.getString(resId), 0);
        } catch(ClassCastException e) {
            int i = Integer.parseInt(sharedPreferences.getString(mContext.getString(resId), "0"));
            editor.putInt(mContext.getString(resId), i);
            editor.commit();
            return i;
        }
    }
    public int setInt(int resId, int val) {
        return setInt(mContext.getString(resId), val);
    }
    public int setInt(String key, int val) {
        editor.putInt(key, val);
        editor.commit();
        return val;
    }
    public long getLong(int resId) {
        try {
            return sharedPreferences.getLong(mContext.getString(resId), 0);
        } catch(ClassCastException e) {
            if(sharedPreferences.getString(mContext.getString(resId),"").isEmpty())
                return 0;
            long l;
            try {
                l = Long.parseLong(sharedPreferences.getString(mContext.getString(resId), "0"));
            } catch(ClassCastException e2) {
                l = 0;
            }
            editor.putLong(mContext.getString(resId), l);
            editor.commit();
            return l;
        }
    }
    public long setLong(int resId, long val) {
        return setLong(mContext.getString(resId), val);
    }
    public long setLong(String key, long val) {
        editor.putLong(key, val);
        editor.commit();
        return val;
    }

    //Default Stuff
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getDefaultSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    /**
     * Takes saved game data that is in the JSON format and updates SharedPreferences
     * @param json JSONObject containing SharedPreferences in key->value pairs
     */
    public void readSnapshot(JSONObject json) {
        try {
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                Object v = json.get(key);
                if(v.getClass().toString().contains("Boolean")) {
                    setBoolean(key, (Boolean) v);
                } else if(v.getClass().toString().contains("String")) {
                    if(v.equals("true"))
                        setBoolean(key, true);
                    else if(v.equals("false"))
                        setBoolean(key, false);
                    else
                        setString(key, (String) v);
                } else if (v.getClass().toString().contains("Integer")) {
                    setInt(key, (int) v);
                } else if (v.getClass().toString().contains("Long")) {
                    setLong(key, (long) v);
                }
                //Log.d(TAG, "Read " + dataObject.get(key).getClass().toString() + " " + key + " = " + dataObject.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes saved game data that is in a byte array and updates SharedPreferences
     * @param gameData Saved game data snapshot from Google Drive
     */
    public void readSnapshotAsBytes(byte[] gameData) {
        String dataString = new String(gameData);
        Log.d(TAG, "Found "+dataString);
        try {
            readSnapshot(new JSONObject(dataString));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes all SharedPreferences data to a JSON format
     * @return JSONObject of SharedPreferences
     */
    public JSONObject writeSnapshot() {
        Set<String> keys = sharedPreferences.getAll().keySet();
        JSONObject jsonObject = new JSONObject();
        for(String s: keys) {
            try {
                Object value = sharedPreferences.getAll().get(s);
                if(value.equals("true"))
                    value = true;
                else if(value.equals("false"))
                    value = false;
                try {
                    if(value.equals(Integer.parseInt(value.toString()))) {
                        value = (Integer) value;
                    }
                } catch(NumberFormatException ignored) {}
                //Log.d(TAG, "Write "+value.getClass().toString()+" "+s+" = "+value);
                jsonObject.put(s, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * Writes all SharedPreferenes data to a byte array
     * This is useful for Google Play Games snapshots
     * @return A byte array of SharedPreferences
     */
    public byte[] writeSnapshotToBytes() {
        String s = writeSnapshot().toString();
        return s.getBytes();
    }
}

