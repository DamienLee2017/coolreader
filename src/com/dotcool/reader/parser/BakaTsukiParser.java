/**
 * Parse baka-tsuki wiki page
 */
package com.dotcool.reader.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.dotcool.model.Book;
import com.dotcool.reader.Constants;
import com.dotcool.reader.LNReaderApplication;
import com.dotcool.reader.UIHelper;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.BakaReaderException;
import com.dotcool.reader.helper.Util;
import com.dotcool.reader.model.BookModel;
import com.dotcool.reader.model.ImageModel;
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.NovelContentModel;
import com.dotcool.reader.model.PageModel;

/**
 * @author Nandaka
 * 
 */
public class BakaTsukiParser {

	private static final String TAG = BakaTsukiParser.class.toString();
	//最近新品小说列表部分
	private static List<Book> parseBook(String  jsonString) throws JSONException {
	    JSONArray array = new JSONArray(jsonString);
        List<Book> items = new ArrayList<Book>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject data = array.getJSONObject(i);
            Book result = new Book();
            result.setBookId(data.optInt("id"));
            result.setBookName(data.optString("title"));
            result.setBookAuthor(data.optString("author"));
            result.setBookImgUrl(data.optString("imgurl"));
            result.setBookSummary(data.optString("summary"));
            result.setBookCreateTime(data.optLong("createtime"));
            result.setBookUpdateTime(data.optLong("updatetime"));
            result.setBookHits(data.optInt("hits"));
            result.setBookLastChapterID(data.optString("lastchapterid"));
            result.setBookLastChapterTitle(data.optString("lastchaptertitle"));
            result.setBookChapterCount(data.optInt("chaptercount"));
            items.add(result);
        }
        return items;
    }
	public static ArrayList<PageModel> ParseNovelList(String doc) throws JSONException {
		ArrayList<PageModel> result = new ArrayList<PageModel>();
	
		List<Book> items= parseBook(doc);

			int order = 0;
			for (Iterator<Book> i = items.iterator(); i.hasNext();) {
				Book novel = i.next();
				PageModel page = new PageModel();
				int tempPage = novel.getBookId();
				page.setSummary(novel.getBookSummary());
				page.setImgurl(novel.getBookImgUrl());
				page.setId(tempPage);
				page.setLanguage(Constants.LANG_ENGLISH);
				page.setType(PageModel.TYPE_NOVEL);
				page.setTitle(novel.getBookName());
				page.setPage(novel.getBookName());
				page.setLastUpdate(new Date(novel.getBookUpdateTime() * 1000)); // set to min value if never open
				page.setLastCheck(new Date(novel.getBookCreateTime() * 1000));
				try {
					// get the saved data if available
					PageModel temp = NovelsDao.getInstance().getPageModel(page, null, false);
					if (temp != null) {
						page.setWatched(temp.isWatched());
						page.setFinishedRead(temp.isFinishedRead());
						page.setDownloaded(temp.isDownloaded());
					}
				} catch (Exception e) {
					Log.e(TAG, "发生错误: " + page.getPage(), e);
				}
				page.setParent("Main_Page");
				page.setOrder(order);
				result.add(page);
				
				++order;
			}
		

		return result;
	}
    
	public static ArrayList<PageModel> ParseTeaserList(String doc) throws JSONException {
	    ArrayList<PageModel> result = new ArrayList<PageModel>();
	    
        List<Book> items= parseBook(doc);

            int order = 0;
            for (Iterator<Book> i = items.iterator(); i.hasNext();) {
                Book novel = i.next();
                PageModel page = new PageModel();
                int tempPage = novel.getBookId();
                page.setSummary(novel.getBookSummary());
                page.setImgurl(novel.getBookImgUrl());
                page.setId(tempPage);
                page.setLanguage(Constants.LANG_ENGLISH);
                page.setType(PageModel.TYPE_NOVEL);
                page.setTitle(novel.getBookName());
                page.setPage(novel.getBookName());
                page.setLastUpdate(new Date(0)); // set to min value if never open
                try {
                    // get the saved data if available
                    PageModel temp = NovelsDao.getInstance().getPageModel(page, null, false);
                    if (temp != null) {
                        page.setLastUpdate(temp.getLastUpdate());
                        page.setWatched(temp.isWatched());
                        page.setFinishedRead(temp.isFinishedRead());
                        page.setDownloaded(temp.isDownloaded());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "发生错误: " + page.getPage(), e);
                }
                page.setLastCheck(new Date());
                page.setParent("Main_Page");
                page.setOrder(order);
                result.add(page);
                
                ++order;
            }
        

        return result;
	}


	public static ArrayList<PageModel> ParseOriginalList(String doc) throws JSONException {
	    ArrayList<PageModel> result = new ArrayList<PageModel>();
        
         List<Book> items= parseBook(doc);

            int order = 0;
            for (Iterator<Book> i = items.iterator(); i.hasNext();) {
                Book novel = i.next();
                PageModel page = new PageModel();
                int tempPage = novel.getBookId();
                page.setSummary(novel.getBookSummary());
                page.setImgurl(novel.getBookImgUrl());
                page.setId(tempPage);
                page.setLanguage(Constants.LANG_ENGLISH);
                page.setType(PageModel.TYPE_NOVEL);
                page.setTitle(novel.getBookName());
                page.setPage(novel.getBookName());
                page.setLastUpdate(new Date(0)); // set to min value if never open
                try {
                    // get the saved data if available
                    PageModel temp = NovelsDao.getInstance().getPageModel(page, null, false);
                    if (temp != null) {
                        page.setLastUpdate(temp.getLastUpdate());
                        page.setWatched(temp.isWatched());
                        page.setFinishedRead(temp.isFinishedRead());
                        page.setDownloaded(temp.isDownloaded());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "发生错误: " + page.getPage(), e);
                }
                page.setLastCheck(new Date());
                page.setParent("Main_Page");
                page.setOrder(order);
                result.add(page);
                
                ++order;
            }
        

        return result;
	}

	private static ArrayList<BookModel> parseDetails(String  jsonString) throws JSONException {
        JSONArray array = new JSONArray(jsonString);
        ArrayList<BookModel> items = new ArrayList<BookModel>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject data = array.getJSONObject(i);
            BookModel result = new BookModel();
            result.setId(data.optInt("id"));
            result.setTitle(data.optString("title"));
            result.setBookId(data.optInt("bookid"));
            result.setChapter(data.optInt("chapter"));
            result.setLastUpdate(new Date(data.optLong("createtime") * 1000));
            result.setLastCheck(new Date(data.optLong("updatetime") * 1000));  
            ArrayList<PageModel> pages=new ArrayList<PageModel>();
            PageModel page = new PageModel();
            page.setBook(result);
            page.setDownloaded(false);
            page.setId(result.getId());
            //page.setLastUpdate(new Date(result.getUpdatetime()));
            page.setPage(result.getTitle());
            pages.add(page);
            result.setChapterCollection(pages);
            items.add(result);
        }
        return items;
    }
	public static NovelCollectionModel ParseNovelDetails(String doc, PageModel page) throws MalformedURLException, JSONException {
		NovelCollectionModel novel = new NovelCollectionModel();
		if (doc == null)
			throw new NullPointerException("Document cannot be null.");
		novel.setPage(page.getPage());
		novel.setPageModel(page);

		String redirected = null;
		novel.setRedirectTo(redirected);

		novel.setSynopsis(page.getTitle());
		URL url = new URL(Constants.BASE_IMAGE_URL+page.getImgurl());
        novel.setCoverUrl(url);
        ArrayList<BookModel> books = parseDetails(doc);
        novel.setBookCollections(books);

		return novel;
	}

  //获取小说内容
	public static NovelContentModel ParseNovelContent(String doc, BookModel page) throws Exception {
		NovelContentModel content = new NovelContentModel();
		content.setPage(page.getPage());
		JSONObject data = new JSONObject(doc);
		content.setContent(data.optString("content"));
		content.setLastXScroll(0);
		content.setLastYScroll(0);
		content.setLastZoom(Constants.DISPLAY_SCALE);
		return content;
	}
}
