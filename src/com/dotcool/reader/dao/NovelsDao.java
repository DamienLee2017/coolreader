/**
 *
 */
package com.dotcool.reader.dao;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.dotcool.R;
import com.dotcool.reader.AlternativeLanguageInfo;
import com.dotcool.reader.Constants;
import com.dotcool.reader.LNReaderApplication;
import com.dotcool.reader.UIHelper;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.helper.BakaReaderException;
import com.dotcool.reader.helper.DBHelper;
import com.dotcool.reader.helper.Util;
import com.dotcool.reader.helper.db.BookModelHelper;
import com.dotcool.reader.helper.db.BookmarkModelHelper;
import com.dotcool.reader.helper.db.ImageModelHelper;
import com.dotcool.reader.helper.db.NovelCollectionModelHelper;
import com.dotcool.reader.helper.db.NovelContentModelHelper;
import com.dotcool.reader.helper.db.PageModelHelper;
import com.dotcool.reader.helper.db.UpdateInfoModelHelper;
import com.dotcool.reader.model.BookModel;
import com.dotcool.reader.model.BookmarkModel;
import com.dotcool.reader.model.ImageModel;
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.NovelContentModel;
import com.dotcool.reader.model.PageModel;
import com.dotcool.reader.model.UpdateInfoModel;
import com.dotcool.reader.parser.BakaTsukiParser;
import com.dotcool.reader.parser.BakaTsukiParserAlternative;
import com.dotcool.reader.parser.CommonParser;
import com.dotcool.reader.task.DownloadFileTask;
import com.dotcool.util.AppUtil;

/**
 * @author Nandaka
 * 
 */
public class NovelsDao {
	private static final String TAG = NovelsDao.class.toString();
	private static DBHelper dbh;
	private static Context context;

	private static NovelsDao instance;
	private static Object lock = new Object();

	public static NovelsDao getInstance(Context applicationContext) {
		synchronized (lock) {
			if (instance == null) {
				instance = new NovelsDao(applicationContext);
				context = applicationContext;
			}
		}
		return instance;
	}

	public static NovelsDao getInstance() {
		synchronized (lock) {
			if (instance == null) {
				try {
					instance = new NovelsDao(LNReaderApplication.getInstance().getApplicationContext());
				} catch (Exception ex) {
					Log.e(TAG, "Failed to get context for NovelsDao", ex);
					// throw new BakaReaderException("NovelsDao is not Initialized!", BakaReaderException.NULL_NOVELDAO,
					// ex);
					throw new NullPointerException("NovelsDao is not Initialized!");
				}
			}
		}
		return instance;
	}

	private NovelsDao(Context context) {
		if (dbh == null) {
			dbh = new DBHelper(context);
		}
	}

	public DBHelper getDBHelper() {
		if (dbh == null) {
			dbh = new DBHelper(context);
		}
		return dbh;
	}

	public void deleteDB() {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				dbh.deletePagesDB(db);
			} finally {
				db.close();
			}
		}
	}

	public String copyDB(boolean makeBackup) throws IOException {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			String filePath;
			try {
				filePath = dbh.copyDB(db, context, makeBackup);
			} finally {
				db.close();
			}
			return filePath;
		}
	}

	public ArrayList<PageModel> getNovels(ICallbackNotifier notifier, boolean alphOrder,String api) throws Exception {
		ArrayList<PageModel> list = null;
		PageModel page = null;
		SQLiteDatabase db = null;

		// check if main page exist
		synchronized (dbh) {
			try {
				db = dbh.getReadableDatabase();
				
				if(api.equals("/api/leixin/cat/1")){
				    page = PageModelHelper.getHistoryPage(db);
				}else if(api.equals("/api/leixin/cat/3")){
                    page = PageModelHelper.getKehuanPage(db);
                }else if(api.equals("/api/leixin/cat/7")){
                    page = PageModelHelper.getXuanhuanPage(db);
                }else if(api.equals("/api/leixin/cat/10")){
                    page = PageModelHelper.getQitaPage(db);
                }else if(api.equals("/api/book/update/11")){
                    page = PageModelHelper.getUpdatePage(db);
                }else if(api.equals("/api/book/update/12")){
                    page = PageModelHelper.getMoodPage(db);
                }else {
                    page = PageModelHelper.getMainPage(db);  
                }
				
			} finally {
				db.close();
			}
		}
		if (page == null) {
			Log.d(TAG, "No Main_Page data!");
			list = getNovelsFromInternet(notifier,api);
		} else {
			// get from db
			synchronized (dbh) {
				try {
					db = dbh.getReadableDatabase();
					list = dbh.getAllNovels(db, alphOrder);// dbh.selectAllByColumn(db,
					// DBHelper.COLUMN_TYPE,
					// PageModel.TYPE_NOVEL);
				} finally {
					db.close();
				}
			}
			Log.d(TAG, "Found: " + list.size());
		}

		return list;
	}

	public ArrayList<PageModel> getNovelsFromInternet(ICallbackNotifier notifier,String api) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("没有网络，请稍后...", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("正在加载列表请稍后..."));
		}
		// get last updated main page revision from internet

		PageModel mainPage = new PageModel();
		if(api.equals("/api/leixin/cat/1")){
		    mainPage.setPage("Category:history");
		    mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.str_xuanhuan));
        }else if(api.equals("/api/leixin/cat/3")){
            mainPage.setPage("Category:kehuan");
            mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.str_xuanhuan));
        }else if(api.equals("/api/leixin/cat/7")){
            mainPage.setPage("Category:xuanhuan");
            mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.str_xuanhuan));
        }else if(api.equals("/api/leixin/cat/10")){
            mainPage.setPage("Category:qita");
            mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.str_xuanhuan));
        }else if(api.equals("/api/book/update/11")){
            mainPage.setPage("Category:update");
            mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.light_novels));
        }else if(api.equals("/api/book/mood/12")){
            mainPage.setPage("Category:mood");
            mainPage.setTitle(LNReaderApplication.getInstance().getResources().getString(R.string.originals));
        }
		mainPage.setLanguage(Constants.LANG_ENGLISH);
		mainPage = getPageModel(mainPage, notifier);
		mainPage.setType(PageModel.TYPE_OTHER);
		mainPage.setParent("");

		ArrayList<PageModel> list = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				// db.beginTransaction();
				mainPage = PageModelHelper.insertOrUpdatePageModel(db, mainPage, false);
				Log.d(TAG, "Updated Main_Page");

				// now get the novel list
				list = new ArrayList<PageModel>();

				String url = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + api;
				Log.d(TAG, "--"+url);
				int retry = 0;
				while (retry < getRetry()) {
					try {
						Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
						//Document doc = response.parse();
						String result =response.body();
						//String result =  AppUtil.getContent(url);
						
						Log.d(TAG, "Found from internet: "+ result + " Novels");
						list = BakaTsukiParser.ParseNovelList(result);
						Log.d(TAG, "Found from internet: " + list.size() + " Novels");

						// saved to db and get saved value
						list = PageModelHelper.insertAllNovel(db, list);

						// db.setTransactionSuccessful();

						if (notifier != null) {
							notifier.onCallback(new CallbackEventData("Found: " + list.size() + " novels."));
						}
						break;
					} catch (EOFException eof) {
						++retry;
						if (notifier != null) {
							notifier.onCallback(new CallbackEventData("Retrying: Main_Page (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
						}
						if (retry > getRetry())
							throw eof;
					} catch (IOException eof) {
						++retry;
						String message = "Retrying: Main_Page (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
						if (notifier != null) {
							notifier.onCallback(new CallbackEventData(message));
						}
						Log.d(TAG, message, eof);
						if (retry > getRetry())
							throw eof;
					}
				}
			} finally {
				// db.endTransaction();
				db.close();
			}
		}
		return list;
	}

	public ArrayList<PageModel> getWatchedNovel() {
		ArrayList<PageModel> watchedNovel = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				// watchedNovel = dbh.selectAllByColumn(db,
				// DBHelper.COLUMN_IS_WATCHED + " = ? and ("
				// + DBHelper.COLUMN_PARENT + " = ? or "
				// + DBHelper.COLUMN_PARENT + " = ? )"
				// , new String[] { "1", "Main_Page", "Category:Teasers" }
				// , DBHelper.COLUMN_TITLE );
				watchedNovel = dbh.getAllWatchedNovel(db, true);
			} finally {
				db.close();
			}
		}
		return watchedNovel;
	}

	public ArrayList<PageModel> getTeaser(ICallbackNotifier notifier, boolean alphOrder) throws Exception {
		SQLiteDatabase db = null;
		PageModel page = null;
		ArrayList<PageModel> list = null;
		// check if main page exist
		synchronized (dbh) {
			try {
				db = dbh.getReadableDatabase();
				page = PageModelHelper.getTeaserPage(db);
			} finally {
				db.close();
			}
		}

		if (page == null) {
			return getTeaserFromInternet(notifier);
		} else {
			// get from db
			synchronized (dbh) {
				try {
					db = dbh.getReadableDatabase();
					list = dbh.getAllTeaser(db, alphOrder);
				} finally {
					db.close();
				}
			}
			Log.d(TAG, "Found: " + list.size());
		}

		return list;
	}

	public ArrayList<PageModel> getTeaserFromInternet(ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("No Network Connectifity", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("Downloading Teaser Novels list..."));
		}

		// parse Category:Teasers information
		PageModel teaserPage = new PageModel();
		teaserPage.setPage("Category:Teasers");
		teaserPage.setLanguage(Constants.LANG_ENGLISH);
		teaserPage.setTitle("Teasers");
		teaserPage = getPageModel(teaserPage, notifier);
		teaserPage.setType(PageModel.TYPE_OTHER);

		// update page model
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			teaserPage = PageModelHelper.insertOrUpdatePageModel(db, teaserPage, true);
			Log.d(TAG, "Updated Category:Teasers");
		}

		// get teaser list
		ArrayList<PageModel> list = null;
		String url = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + "/project/index.php?title=Category:Teasers";
		int retry = 0;
		while (retry < getRetry()) {
			try {
				Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
				String doc = response.body();

				list = BakaTsukiParser.ParseTeaserList(doc);
				Log.d(TAG, "Found from internet: " + list.size() + " Teaser");

				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Found: " + list.size() + " teaser."));
				}
				break;
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Retrying: Category:Teasers (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "Retrying: Category:Teasers (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}

		// save teaser list
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			for (PageModel pageModel : list) {
				pageModel = PageModelHelper.insertOrUpdatePageModel(db, pageModel, true);
				Log.d(TAG, "Updated teaser: " + pageModel.getPage());
			}
		}

		return list;
	}

	// Originals, copied from teaser
	public ArrayList<PageModel> getOriginal(ICallbackNotifier notifier, boolean alphOrder) throws Exception {
		SQLiteDatabase db = null;
		PageModel page = null;
		ArrayList<PageModel> list = null;
		// check if main page exist
		synchronized (dbh) {
			try {
				db = dbh.getReadableDatabase();
				page = PageModelHelper.getOriginalPage(db);
			} finally {
				db.close();
			}
		}

		if (page == null) {
			return getOriginalFromInternet(notifier);
		} else {
			// get from db
			synchronized (dbh) {
				try {
					db = dbh.getReadableDatabase();
					list = dbh.getAllOriginal(db, alphOrder);
				} finally {
					db.close();
				}
			}
			Log.d(TAG, "Found: " + list.size());
		}

		return list;
	}

	public ArrayList<PageModel> getOriginalFromInternet(ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new Exception("No Network Connectifity");
		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("Downloading Original Novels list..."));
		}

		// parse Category:Teasers information
		PageModel teaserPage = new PageModel();
		teaserPage.setPage("Category:Original");
		teaserPage.setLanguage(Constants.LANG_ENGLISH);
		teaserPage.setTitle("Original Novels");
		teaserPage = getPageModel(teaserPage, notifier);
		teaserPage.setType(PageModel.TYPE_OTHER);

		// update page model
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			teaserPage = PageModelHelper.insertOrUpdatePageModel(db, teaserPage, true);
			Log.d(TAG, "Updated Category:Original");
		}

		// get teaser list
		ArrayList<PageModel> list = null;
		String url = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + "/project/index.php?title=Category:Original";
		int retry = 0;
		while (retry < getRetry()) {
			try {
				Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
				String doc = response.body();

				list = BakaTsukiParser.ParseOriginalList(doc);
				Log.d(TAG, "Found from internet: " + list.size() + " Teaser");

				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Found: " + list.size() + " original."));
				}
				break;
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Retrying: Category:Original (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "Retrying: Category:Original (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}

		// save original list
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			for (PageModel pageModel : list) {
				pageModel = PageModelHelper.insertOrUpdatePageModel(db, pageModel, true);
				Log.d(TAG, "Updated original: " + pageModel.getPage());
			}
		}

		return list;
	}

	// Alternative Language, copied from Original
	public ArrayList<PageModel> getAlternative(ICallbackNotifier notifier, boolean alphOrder, String language) throws Exception {
		SQLiteDatabase db = null;
		PageModel page = null;
		ArrayList<PageModel> list = null;
		// check if main page exist
		synchronized (dbh) {
			try {
				db = dbh.getReadableDatabase();
				page = PageModelHelper.getAlternativePage(db, language);
			} finally {
				db.close();
			}
		}

		if (page == null) {
			return getAlternativeFromInternet(notifier, language);
		} else {
			// get from db
			synchronized (dbh) {
				try {
					db = dbh.getReadableDatabase();
					list = dbh.getAllAlternative(db, alphOrder, language);
				} finally {
					db.close();
				}
			}
			Log.d(TAG, "Found: " + list.size());
		}

		return list;
	}

	public ArrayList<PageModel> getAlternativeFromInternet(ICallbackNotifier notifier, String language) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("No Network Connectifity", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("Downloading " + language + " Novels list..."));
		}

		// parse information
		PageModel teaserPage = new PageModel();

		if (language != null) {
			teaserPage.setPage(AlternativeLanguageInfo.getAlternativeLanguageInfo().get(language).getCategoryInfo());
			teaserPage.setTitle(language + " Novels");
			teaserPage.setLanguage(language);
		}

		teaserPage = getPageModel(teaserPage, notifier);
		teaserPage.setType(PageModel.TYPE_NOVEL);

		// update page model
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			teaserPage = PageModelHelper.insertOrUpdatePageModel(db, teaserPage, true);
			Log.d(TAG, "Updated " + language);
		}

		// get alternative list
		ArrayList<PageModel> list = null;
		String url = null;
		if (language != null)
			url = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + "/project/index.php?title=" + AlternativeLanguageInfo.getAlternativeLanguageInfo().get(language).getCategoryInfo();
		int retry = 0;
		while (retry < getRetry()) {
			try {
				Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
				Document doc = response.parse();
				list = BakaTsukiParserAlternative.ParseAlternativeList(doc, language);
				Log.d(TAG, "Found from internet: " + list.size() + " " + language + " Novel");

				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Found: " + list.size() + " " + language + " ."));
				}
				break;
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Retrying: " + language + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "Retrying: " + language + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}

		// save teaser list
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			for (PageModel pageModel : list) {
				pageModel = PageModelHelper.insertOrUpdatePageModel(db, pageModel, true);
				Log.d(TAG, "Updated " + language + " novel: " + pageModel.getPage());
			}
		}

		return list;
	}

	/**
	 * Get page model from db. If autoDownload = true, get the pageModel from
	 * internet if not exists.
	 * 
	 * @param page
	 * @param notifier
	 * @param autoDownload
	 * @return
	 * @throws Exception
	 */
	public PageModel getPageModel(PageModel page, ICallbackNotifier notifier, boolean autoDownload) throws Exception {
		PageModel pageModel = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				pageModel = PageModelHelper.getPageModel(db, page.getPage());
			} finally {
				db.close();
			}
		}
		if (pageModel == null && autoDownload) {
			pageModel = getPageModelFromInternet(page, notifier);
		}
		return pageModel;
	}

	/**
	 * Get page model from db. Get the pageModel from internet if not exists.
	 * 
	 * @param page
	 * @param notifier
	 * @return
	 * @throws Exception
	 */
	public PageModel getPageModel(PageModel page, ICallbackNotifier notifier) throws Exception {
		return getPageModel(page, notifier, true);
	}

	/**
	 * Return pageModel, null if not exist.
	 * 
	 * @param page
	 * @param notifier
	 * @return
	 * @throws Exception
	 */
	public PageModel getExistingPageModel(PageModel page, ICallbackNotifier notifier) throws Exception {
		PageModel pageModel = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				pageModel = PageModelHelper.getPageModel(db, page.getPage());
			} finally {
				db.close();
			}
		}

		return pageModel;
	}

	public PageModel getPageModelFromInternet(PageModel page, ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("没有网络连接", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		Log.d(TAG, "PageModel = " + page.getPage());

		int retry = 0;
		while (retry < getRetry()) {
			try {
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("正在加载 "  + " 请稍后"));
				}
				String encodedTitle = Util.UrlEncode(page.getPage());
				String fullUrl = "http://www.baka-tsuki.org/project/api.php?action=query&prop=info&format=xml&redirects=yes&titles=" + encodedTitle;
				Response response = Jsoup.connect(fullUrl).timeout(getTimeout(retry)).execute();
				PageModel pageModel = null;
				String lang = page.getLanguage();
				if (lang != null)
					pageModel = CommonParser.parsePageAPI(page, response.parse(), fullUrl);
				pageModel.setFinishedRead(page.isFinishedRead());
				pageModel.setWatched(page.isWatched());

				synchronized (dbh) {
					// save to db and get saved value
					SQLiteDatabase db = dbh.getWritableDatabase();
					try {
						pageModel = PageModelHelper.insertOrUpdatePageModel(db, pageModel, false);
					} finally {
						db.close();
					}
				}
				return pageModel;
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("重新尝试: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "重新尝试: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}
		return null;
	}

	public PageModel updatePageModel(PageModel page) {
		PageModel pageModel = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				pageModel = PageModelHelper.insertOrUpdatePageModel(db, page, false);
			} finally {
				db.close();
			}
		}
		return pageModel;
	}

	/*
	 * NovelCollectionModel
	 */

	public NovelCollectionModel getNovelDetails(PageModel page, ICallbackNotifier notifier) throws Exception {
		NovelCollectionModel novel = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				novel = NovelCollectionModelHelper.getNovelDetails(db, page.getPage());
			} finally {
				db.close();
			}
		}
		if (novel == null) {
			novel = getNovelDetailsFromInternet(page, notifier);
		}
		return novel;
	}

	public NovelCollectionModel getNovelDetailsFromInternet(PageModel page, ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("没有网络连接", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		Log.d(TAG, "Getting Novel Details from internet: " + page.getPage());
		NovelCollectionModel novel = null;

		int retry = 0;
		while (retry < getRetry()) {
			try {
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("正在加载小说详细信息: " + page.getPage()));
				}
				//String encodedTitle = Util.UrlEncode();
				String fullUrl = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + "/api/article/" + page.getId()+"/1";
				Response response = Jsoup.connect(fullUrl).timeout(getTimeout(retry)).execute();
				String doc = response.body();
				/*
				 * Add your section of alternative language here, create own
				 * parser for each language for modularity reason
				 */
				if (!page.getLanguage().equals(Constants.LANG_ENGLISH))
					novel = BakaTsukiParserAlternative.ParseNovelDetails(doc, page);
				else
					novel = BakaTsukiParser.ParseNovelDetails(doc, page);
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("重新尝试: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "重新尝试: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}

			// Novel details' Page Model
			if (novel != null) {
				page = updateNovelDetailsPageModel(page, notifier, novel);

				synchronized (dbh) {
					// insert to DB and get saved value
					SQLiteDatabase db = dbh.getWritableDatabase();
					try {
						db.beginTransaction();
						novel = NovelCollectionModelHelper.insertNovelDetails(db, novel);
						db.setTransactionSuccessful();
						//db.endTransaction();
					} finally {
						db.endTransaction();
						db.close();
					}
				}

				// update info for each chapters
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("获取章节信息: " + page.getPage()));
				}
				Log.d(TAG, "Cover size: " + novel.getFlattedChapterList().size());
				ArrayList<PageModel> chapters = getUpdateInfo(novel.getFlattedChapterList(), notifier);
				
				for (PageModel pageModel : chapters) {
					pageModel = updatePageModel(pageModel);
				}

				// download cover image
				Log.d(TAG, "Cover imgurl: " + page.getImgurl());
				novel.setCoverUrl(new URL(Constants.BASE_IMAGE_URL+ page.getImgurl()));
				if (novel.getCoverUrl() != null) {
					if (notifier != null) {
						notifier.onCallback(new CallbackEventData("获取封面图片."));
					}
					DownloadFileTask task = new DownloadFileTask(notifier);
					ImageModel image = task.downloadImage(novel.getCoverUrl());
					// TODO: need to save to db?
					Log.d(TAG, "Cover Image: " + image.toString());
					novel.setCover(image.getDedepath());
					
					synchronized (dbh) {
	                    // insert to DB and get saved value
	                    SQLiteDatabase db = dbh.getWritableDatabase();
	                    try {
	                        db.beginTransaction();
	                        NovelCollectionModelHelper.insertNovelDetailsImg(db, novel);
	                        db.setTransactionSuccessful();
	                        //db.endTransaction();
	                    } finally {
	                        db.endTransaction();
	                        db.close();
	                    }
	                }
					
					
				}

				Log.d(TAG, "Complete getting Novel Details from internet: " + page.getPage());
				break;
			}
		}
		return novel;
	}

	public PageModel updateNovelDetailsPageModel(PageModel page, ICallbackNotifier notifier, NovelCollectionModel novel) throws Exception {
		// comment out because have teaser now...
		// page.setParent("Main_Page"); // insurance

		// get the last update time from internet
		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("获取小说信息: " + page.getPage()));
		}
		/*PageModel novelPageTemp = getPageModelFromInternet(page, notifier);
		if (novelPageTemp != null) {
			page.setLastUpdate(novelPageTemp.getLastUpdate());
			page.setLastCheck(new Date());
			novel.setLastUpdate(novelPageTemp.getLastUpdate());
			novel.setLastCheck(new Date());
		} else {
			page.setLastUpdate(new Date(0));
			page.setLastCheck(new Date());
			novel.setLastUpdate(new Date(0));
			novel.setLastCheck(new Date());
		}*/
		// save the changes
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				page = PageModelHelper.insertOrUpdatePageModel(db, page, true);
			} finally {
				db.close();
			}
		}
		return page;
	}

	public PageModel getUpdateInfo(PageModel pageModel, ICallbackNotifier notifier) throws Exception {
		ArrayList<PageModel> pageModels = new ArrayList<PageModel>();
		pageModels.add(pageModel);
		pageModels = getUpdateInfo(pageModels, notifier);
		return pageModels.get(0);
	}

	/***
	 * Bulk update page info through wiki API - LastUpdateInfo. - Redirected. -
	 * Missing flag.
	 * 
	 * @param pageModels
	 * @param notifier
	 * @return
	 * @throws Exception
	 */
	public ArrayList<PageModel> getUpdateInfo(ArrayList<PageModel> pageModels, ICallbackNotifier notifier) throws Exception {
		ArrayList<PageModel> resultPageModel = new ArrayList<PageModel>();
		ArrayList<PageModel> noInfoPageModel = new ArrayList<PageModel>();
		ArrayList<PageModel> externalPageModel = new ArrayList<PageModel>();

		String baseUrl = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + "/project/api.php?action=query&prop=info&format=xml&redirects=yes&titles=";
		int i = 0;
		int retry = 0;
		while (i < pageModels.size()) {
			int apiPageCount = 1;
			ArrayList<PageModel> checkedPageModel = new ArrayList<PageModel>();
			String titles = "";

			while (i < pageModels.size() && apiPageCount < 50) {
				if (pageModels.get(i).isExternal()) {
					pageModels.get(i).setMissing(false);
					externalPageModel.add(pageModels.get(i));
					++i;
					continue;
				}
				if (pageModels.get(i).isMissing() || pageModels.get(i).getPage().endsWith("&action=edit&redlink=1")) {
					pageModels.get(i).setMissing(true);
					noInfoPageModel.add(pageModels.get(i));
					++i;
					continue;
				}
				if (titles.length() + pageModels.get(i).getPage().length() < 2000) {
					titles += "|" + Util.UrlEncode(pageModels.get(i).getPage());
					checkedPageModel.add(pageModels.get(i));
					++i;
					++apiPageCount;
				} else {
					break;
				}
			}

			// request the page
			while (retry < getRetry()) {
				try {
					// Log.d(TAG, "Trying to get: " + baseUrl + titles);
					String url = baseUrl + titles;
					Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
					Document doc = response.parse();
					ArrayList<PageModel> updatedPageModels = CommonParser.parsePageAPI(checkedPageModel, doc, url);
					resultPageModel.addAll(updatedPageModels);
					break;
				} catch (EOFException eof) {
					++retry;
					if (notifier != null) {
						notifier.onCallback(new CallbackEventData("Retrying: Get Pages Info (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
					}
					if (retry > getRetry())
						throw eof;
				} catch (IOException eof) {
					++retry;
					String message = "Retrying: Get Pages Info (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
					if (notifier != null) {
						notifier.onCallback(new CallbackEventData(message));
					}
					Log.d(TAG, message, eof);
					if (retry > getRetry())
						throw eof;
				}
			}
		}

		for (PageModel page : externalPageModel) {
			getExternalUpdateInfo(page);
		}

		resultPageModel.addAll(noInfoPageModel);
		resultPageModel.addAll(externalPageModel);

		return resultPageModel;
	}

	public void getExternalUpdateInfo(PageModel page) {
		int retry;
		Map<String, String> headers = null;
		// Date: Wed, 13 Nov 2013 13:08:35 GMT
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.US);
		retry = 0;
		while (retry < getRetry()) {
			try {
				headers = Jsoup.connect(page.getPage()).timeout(getTimeout(retry)).maxBodySize(1).execute().headers();
				break;
			} catch (Exception e) {
				Log.e(TAG, "Error when getting updated date for: " + page.getPage(), e);
				++retry;
			}
		}
		if (headers != null) {
			String dateStr = null;
			if (headers.containsKey("Last-Modified")) {
				dateStr = headers.get("Last-Modified");
			}
			else if (headers.containsKey("Date")) {
				dateStr = headers.get("Date");
			}
			if (!Util.isStringNullOrEmpty(dateStr)) {
				try {
					Log.d(TAG, "External Novel last update: " + dateStr);
					page.setLastUpdate(df.parse(dateStr));
				} catch (Exception e) {
					Log.e(TAG, "Failed to parse date for: " + page.getPage(), e);
				}
			}
		}
		page.setLastCheck(new Date());
	}

	public void deleteBooks(BookModel bookDel) {
		synchronized (dbh) {
			// get from db
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				BookModel tempBook = BookModelHelper.getBookModel(db, bookDel.getId());
				if (tempBook != null) {
					ArrayList<PageModel> chapters = tempBook.getChapterCollection();
					for (PageModel chapter : chapters) {
						NovelContentModelHelper.deleteNovelContent(db, chapter);
					}
					BookModelHelper.deleteBookModel(db, tempBook);
				}
			} finally {
				db.close();
			}
		}
	}

	public void deletePage(PageModel page) {
		synchronized (dbh) {
			// get from db
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				PageModel tempPage = PageModelHelper.getPageModel(db, page.getId());
				if (tempPage != null) {
					PageModelHelper.deletePageModel(db, tempPage);
				}
			} finally {
				db.close();
			}
		}
	}

	public ArrayList<PageModel> getChapterCollection(String page, String title, BookModel book) {
		synchronized (dbh) {
			// get from db
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				return NovelCollectionModelHelper.getChapterCollection(db, page + Constants.NOVEL_BOOK_DIVIDER + title, book);
			} finally {
				db.close();
			}
		}
	}

	public ArrayList<PageModel> getAllContentPageModel() {
		ArrayList<PageModel> result = null;
		synchronized (dbh) {
			// get from db
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				result = PageModelHelper.getAllContentPageModel(db);
			} finally {
				db.close();
			}
		}
		return result;
	}

	/*
	 * NovelContentModel
	 */

	public NovelContentModel getNovelContent(BookModel page, boolean download, ICallbackNotifier notifier) throws Exception {
		NovelContentModel content = null;

		synchronized (dbh) {
			// get from db
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				content = NovelContentModelHelper.getNovelContent(db, page.getPage());
			} finally {
				db.close();
			}
		}
		// get from Internet;
		if (download && content == null) {
			Log.d("getNovelContent", "Get from Internet: " + page.getPage());
			content = getNovelContentFromInternet(page, notifier);
		}
		Log.d("getNovelContent", "Text: " + content.getContent());
		return content;
	}

	public NovelContentModel getNovelContentFromInternet(BookModel page, ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("没有网络连接", BakaReaderException.NO_NETWORK_CONNECTIFITY);

		String oldTitle = page.getTitle();

		NovelContentModel content = new NovelContentModel();
		int retry = 0;
		String doc = null;
		while (retry < getRetry()) {
			try {
				String encodedUrl = Constants.BASE_URL + "/api/article/"+page.getBookId()+"/ch/"+page.getChapter();
				Response response = Jsoup.connect(encodedUrl).timeout(getTimeout(retry)).execute();
				doc = response.body();
				
				content = BakaTsukiParser.ParseNovelContent(doc, page);
				content.setUpdatingFromInternet(true);
				break;
			} catch (EOFException eof) {
				++retry;
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Retrying: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "Retrying: " + page.getPage() + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}
		/*if (doc != null) {
			// download all attached images
			DownloadFileTask task = new DownloadFileTask(notifier);
			for (ImageModel image : content.getImages()) {
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Start downloading: " + image.getUrl()));
				}
				image = task.downloadImage(image.getUrl());
				// TODO: need to save image to db? mostly thumbnail only
			}

			// download linked big images
			boolean isDownloadBigImage = PreferenceManager.getDefaultSharedPreferences(LNReaderApplication.getInstance()).getBoolean(Constants.PREF_DOWLOAD_BIG_IMAGE, false);
			if (isDownloadBigImage) {
				Document imageDoc = Jsoup.parse(content.getContent());
				ArrayList<String> images = CommonParser.parseImagesFromContentPage(imageDoc);
				for (String imageUrl : images) {
					// ImageModel bigImage = getImageModelFromInternet(image, notifier);
					ImageModel bigImage = new ImageModel();
					bigImage.setBigImage(true);
					bigImage.setName(imageUrl);
					bigImage.setReferer(imageUrl);
					bigImage = getImageModel(bigImage, notifier);
				}
			}

			// get last updated info

			PageModel contentPageModelTemp = getPageModelFromInternet(content.getPageModel(), notifier);
			if (contentPageModelTemp != null) {
				// overwrite the old title
				content.getPageModel().setTitle(oldTitle);
				// syncronize the date
				content.getPageModel().setLastUpdate(contentPageModelTemp.getLastUpdate());
				content.getPageModel().setLastCheck(new Date());
				content.setLastUpdate(contentPageModelTemp.getLastUpdate());
				content.setLastCheck(new Date());
			}

			// page model will be also saved in insertNovelContent()
			synchronized (dbh) {
				// save to DB, and get the saved value
				SQLiteDatabase db = dbh.getWritableDatabase();
				try {
					// TODO: somehow using transaction cause problem...
					db.beginTransaction();
					content = NovelContentModelHelper.insertNovelContent(db, content);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
					db.close();
				}
			}
		}*/
		return content;
	}

	public NovelContentModel updateNovelContent(NovelContentModel content) throws Exception {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				content = NovelContentModelHelper.insertNovelContent(db, content);
			} finally {
				db.close();
			}
		}
		return content;
	}

	public boolean deleteNovelContent(PageModel ref) {
		boolean result = false;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				result = NovelContentModelHelper.deleteNovelContent(db, ref);
			} finally {
				db.close();
			}
		}
		return result;
	}

	/*
	 * ImageModel
	 */

	/**
	 * Get image from db, if not exist will try to download from internet
	 * 
	 * @param image
	 * @param notifier
	 * @return
	 * @throws Exception
	 */
	public ImageModel getImageModel(ImageModel image, ICallbackNotifier notifier) throws Exception {
		if (image == null || image.getName() == null)
			throw new BakaReaderException("Empty Image!", BakaReaderException.EMPTY_IMAGE);
		ImageModel imageTemp = null;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			try {
				imageTemp = ImageModelHelper.getImage(db, image);

				if (imageTemp == null) {
					if (image.getReferer() == null)
						image.setReferer(image.getName());
					Log.d(TAG, "Image not found, might need to check by referer: " + image.getName() + ", referer: " + image.getReferer());
					imageTemp = ImageModelHelper.getImageByReferer(db, image);
				}
			} finally {
				db.close();
			}
		}
		boolean downloadBigImage = false;
		if (imageTemp == null) {
			Log.i(TAG, "Image not found in DB, getting data from internet: " + image.getName());
			downloadBigImage = true;
		} else if (!new File(imageTemp.getPath()).exists()) {
			try{
				Log.i(TAG, "Image found in DB, but doesn't exist in path: " + imageTemp.getPath()
						+"\nAttempting URLDecoding method with default charset:"+java.nio.charset.Charset.defaultCharset().displayName());
				if(!new File(java.net.URLDecoder.decode(imageTemp.getPath(), java.nio.charset.Charset.defaultCharset().displayName())).exists()){
					Log.i(TAG, "Image found in DB, but doesn't exist in URL decoded path: " + java.net.URLDecoder.decode(imageTemp.getPath(), java.nio.charset.Charset.defaultCharset().displayName()));
					downloadBigImage = true;					
				} //else Log.i(TAG, "Image found in DB with URL decoded path: " + java.net.URLDecoder.decode(imageTemp.getPath(), java.nio.charset.Charset.defaultCharset().displayName()));
				
			}catch(Exception e){
				Log.i(TAG, "Image found in DB, but path string seems to be broken: " + imageTemp.getPath()
						+ " Charset:" + java.nio.charset.Charset.defaultCharset().displayName());
				downloadBigImage = true;
			}
		}
		if (downloadBigImage) {
			Log.d(TAG, "Downloading big image from internet: " + image.getName());
			imageTemp = getImageModelFromInternet(image, notifier);
		}

		return imageTemp;
	}

	/**
	 * Get image from internet from File:xxx
	 * 
	 * @param page
	 * @param notifier
	 * @return
	 * @throws Exception
	 */
	public ImageModel getImageModelFromInternet(ImageModel image, ICallbackNotifier notifier) throws Exception {
		if (!LNReaderApplication.getInstance().isOnline())
			throw new BakaReaderException("No Network Connectifity", BakaReaderException.NO_NETWORK_CONNECTIFITY);
		String url = image.getName();
		if (!url.startsWith("http"))
			url = UIHelper.getBaseUrl(LNReaderApplication.getInstance().getApplicationContext()) + url;

		if (notifier != null) {
			notifier.onCallback(new CallbackEventData("Parsing File Page: " + url));
		}

		int retry = 0;
		while (retry < getRetry()) {
			try {
				Response response = Jsoup.connect(url).timeout(getTimeout(retry)).execute();
				Document doc = response.parse();

				// only return the full image url
				image = CommonParser.parseImagePage(doc);

				DownloadFileTask downloader = new DownloadFileTask(notifier);
				image = downloader.downloadImage(image.getUrl());
				image.setReferer(url);

				image = insertImage(image);
				break;
			} catch (EOFException eof) {
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData("Retrying: " + url + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage()));
				}
				++retry;
				if (retry > getRetry())
					throw eof;
			} catch (IOException eof) {
				++retry;
				String message = "Retrying: " + url + " (" + retry + " of " + getRetry() + ")\n" + eof.getMessage();
				if (notifier != null) {
					notifier.onCallback(new CallbackEventData(message));
				}
				Log.d(TAG, message, eof);
				if (retry > getRetry())
					throw eof;
			}
		}
		return image;
	}

	public ImageModel insertImage(ImageModel image) {
		synchronized (dbh) {
			// save to db and get the saved value
			SQLiteDatabase db = dbh.getWritableDatabase();
			try {
				image = ImageModelHelper.insertImage(db, image);
			} finally {
				db.close();
			}
		}
		return image;
	}

	public ArrayList<ImageModel> getAllImages() {
		ArrayList<ImageModel> result;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			result = ImageModelHelper.getAllImages(db);
		}
		return result;
	}

	public ArrayList<PageModel> doSearch(String searchStr, boolean isNovelOnly, ArrayList<String> languageList) {
		if (searchStr == null || searchStr.length() < 3)
			return null;

		ArrayList<PageModel> result;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			result = dbh.doSearch(db, searchStr, isNovelOnly, languageList);
		}
		return result;
	}

	public boolean deleteNovel(PageModel novel) {
		boolean result = false;
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			result = NovelCollectionModelHelper.deleteNovel(db, novel);
		}
		return result;
	}

	public ArrayList<BookmarkModel> getBookmarks(PageModel novel) {
		ArrayList<BookmarkModel> bookmarks = new ArrayList<BookmarkModel>();
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			bookmarks = BookmarkModelHelper.getBookmarks(db, novel);
		}
		return bookmarks;
	}

	public int addBookmark(BookmarkModel bookmark) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			return BookmarkModelHelper.insertBookmark(db, bookmark);
		}
	}

	public int deleteBookmark(BookmarkModel bookmark) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			return BookmarkModelHelper.deleteBookmark(db, bookmark);
		}
	}

	public ArrayList<BookmarkModel> getAllBookmarks(boolean isOrderByDate) {
		ArrayList<BookmarkModel> bookmarks = new ArrayList<BookmarkModel>();
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			bookmarks = BookmarkModelHelper.getAllBookmarks(db, isOrderByDate);
		}
		return bookmarks;
	}

	public boolean isContentUpdated(PageModel page) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			return dbh.isContentUpdated(db, page);
		}
	}

	public int isNovelUpdated(PageModel page) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			return dbh.isNovelUpdated(db, page);
		}
	}

	public ArrayList<UpdateInfoModel> getAllUpdateHistory() {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getReadableDatabase();
			return UpdateInfoModelHelper.getAllUpdateHistory(db);
		}
	}

	public void deleteAllUpdateHistory() {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			UpdateInfoModelHelper.deleteAllUpdateHistory(db);
		}
	}

	public void deleteUpdateHistory(UpdateInfoModel updateInfo) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			UpdateInfoModelHelper.deleteUpdateHistory(db, updateInfo);
		}
	}

	public void insertUpdateHistory(UpdateInfoModel update) {
		synchronized (dbh) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			UpdateInfoModelHelper.insertUpdateHistory(db, update);
		}
	}

	public void deleteChapterCache(PageModel chapter) {
		deleteNovelContent(chapter);

		// Set isDownloaded to false
		chapter.setDownloaded(false);
		updatePageModel(chapter);
	}

	public void deleteBookCache(BookModel bookDel) {
		for (PageModel p : bookDel.getChapterCollection()) {
			deleteChapterCache(p);
		}
	}

	private int getRetry() {
		return UIHelper.getIntFromPreferences(Constants.PREF_RETRY, 3);
	}

	private int getTimeout(int retry) {
		boolean increaseRetry = PreferenceManager.getDefaultSharedPreferences(LNReaderApplication.getInstance().getApplicationContext()).getBoolean(Constants.PREF_INCREASE_RETRY, false);
		int timeout = UIHelper.getIntFromPreferences(Constants.PREF_TIMEOUT, 60) * 1000;
		if (increaseRetry) {
			timeout = timeout * (retry + 1);
		}
		return timeout;
	}
	// public void temp() {
	// synchronized (dbh) {
	// SQLiteDatabase db = dbh.getWritableDatabase();
	// db.execSQL("DROP TABLE IF EXISTS " + dbh.TABLE_NOVEL_BOOKMARK);
	// db.execSQL(dbh.DATABASE_CREATE_NOVEL_BOOKMARK);
	// }
	// }
}