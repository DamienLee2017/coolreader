package com.dotcool.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import android.util.Log;

/*
 * Author : freedomofkeima
 */

public class AlternativeLanguageInfo {

	/* List of attributes */
	private static final String TAG = AlternativeLanguageInfo.class.toString();
	private String language;
	private String category;
	private String markerSynopsis; /* Marker for Synopsis */
	private ArrayList<String> parserInfo;

	private static Hashtable<String, AlternativeLanguageInfo> instance = null; //new Hashtable<String, AlternativeLanguageInfo> ();
	private static Object lock = new Object();

	/* List of methods */
	public static void initHashMap() {
		synchronized (lock) {
			// construct HashTable and populate with proper data with language as the key
			if(instance == null) instance = new Hashtable<String, AlternativeLanguageInfo> ();

		
		    Log.d(TAG,"Polish Language added");
		}
	}

	public static Hashtable<String, AlternativeLanguageInfo> getAlternativeLanguageInfo() {
		synchronized (lock) {
			/* if instance is null, then initHashMap */
		   if(instance == null || instance.isEmpty()) initHashMap();
		   return instance;
		}
	}

	public AlternativeLanguageInfo(String _language, String _category, String _markerSynopsis, ArrayList<String> _parserInfo) {
	  // set the member variables
	  language = _language;
	  category = _category;
	  markerSynopsis = _markerSynopsis;
	  parserInfo = _parserInfo;
	}

	/* Setter & Getter */
	public String getLanguage(){
		return language;
	}

	public String getCategory(){
		return category;
	}

	public String getCategoryInfo(){
		return "Category:" + category;
	}

	public String getMarkerSynopsis(){
		return markerSynopsis;
	}

	public ArrayList<String> getParserInfo(){
		return parserInfo;
	}

	public void setLanguage(String _language){
		language = _language;
	}

	public void setCategory(String _category){
		category = _category;
	}

	public void setMarkerSynopsis(String _markerSynopsis){
		markerSynopsis = _markerSynopsis;
	}

	public void setParserInfo(ArrayList<String> _parserInfo){
		parserInfo = _parserInfo;
	}

}