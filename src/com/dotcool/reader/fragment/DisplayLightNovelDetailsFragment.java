package com.dotcool.reader.fragment;

import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.dotcool.R;
import com.dotcool.reader.Constants;
import com.dotcool.reader.LNReaderApplication;
import com.dotcool.reader.UIHelper;
import com.dotcool.reader.adapter.BookModelAdapter;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.helper.Util;
import com.dotcool.reader.model.BookModel;
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.NovelContentModel;
import com.dotcool.reader.model.PageModel;
import com.dotcool.reader.task.DownloadNovelContentTask;
import com.dotcool.reader.task.IAsyncTaskOwner;
import com.dotcool.reader.task.LoadNovelDetailsTask;
import com.dotcool.view.BookReadActivity;

public class DisplayLightNovelDetailsFragment extends SherlockFragment implements IAsyncTaskOwner {
	public static final String TAG = DisplayLightNovelDetailsFragment.class.toString();
	private PageModel page;
	private NovelCollectionModel novelCol;
	private final NovelsDao dao = NovelsDao.getInstance(getSherlockActivity());

	private BookModelAdapter bookModelAdapter;
	private ExpandableListView expandList;

	private DownloadNovelContentTask downloadTask = null;
	private LoadNovelDetailsTask task = null;

	private TextView loadingText;
	private ProgressBar loadingBar;
	private String touchedForDownload;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		UIHelper.SetActionBarDisplayHomeAsUp(getSherlockActivity(), true);
		View view = inflater.inflate(R.layout.activity_display_light_novel_details, container, false);

		// Get intent and message
		page = new PageModel();
		String pageTitle = getArguments().getString(Constants.EXTRA_PAGE);
		if (Util.isStringNullOrEmpty(pageTitle)) {
			Log.w(TAG, "Page title is empty!");
		}
		page.setPage(pageTitle);

		try {
			page = NovelsDao.getInstance(getSherlockActivity()).getPageModel(page, null);
		} catch (Exception e) {
			Log.e(TAG, "Error when getting Page Model for " + page.getPage(), e);
		}

		loadingText = (TextView) view.findViewById(R.id.emptyList);
		loadingBar = (ProgressBar) view.findViewById(R.id.empttListProgress);

		// setup listener
		expandList = (ExpandableListView) view.findViewById(R.id.chapter_list);
		registerForContextMenu(expandList);
		expandList.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (novelCol != null) {
					PageModel chapter = bookModelAdapter.getChild(groupPosition, childPosition);
					String bookName = novelCol.getBookCollections().get(groupPosition).getTitle();
					touchedForDownload = bookName + " " + chapter.getTitle();
					//loadChapter(chapter);
				}
				return false;
			}
		});
		expandList.setOnGroupClickListener(new OnGroupClickListener(){

            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                    long id) {
                if (novelCol != null) {
                    BookModel chapter = bookModelAdapter.getGroup(groupPosition);
                    loadCotent(chapter);
                  
                }
                    
                return false;
            }
            
        });
		getSherlockActivity().setTitle(page.getTitle());

		setHasOptionsMenu(true);

		executeTask(page, false);
		return view;
	}
    private void loadCotent(BookModel chapter) {

        downloadTask = new DownloadNovelContentTask(chapter, DisplayLightNovelDetailsFragment.this);
        downloadTask.execute();
  
    }

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume: " + task.getStatus().toString());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_display_light_novel_details, menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		Log.d(TAG, "menu Option called.");
		switch (item.getItemId()) {
		case R.id.menu_refresh_chapter_list:
			Log.d(TAG, "Refreshing Details");
			executeTask(page, true);
			Toast.makeText(getSherlockActivity(), getResources().getString(R.string.refreshing_detail), Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		MenuInflater inflater = getSherlockActivity().getMenuInflater();
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			inflater.inflate(R.menu.novel_details_volume_context_menu, menu);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			inflater.inflate(R.menu.novel_details_chapter_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (!(item.getMenuInfo() instanceof ExpandableListView.ExpandableListContextMenuInfo))
			return super.onContextItemSelected(item);
		Log.d(TAG, "Context menu called");

		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		// unpacking
		int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

		BookModel chapter = null;

		switch (item.getItemId()) {
		// Volume cases
		case R.id.clear_volume:

			/*
			 * Implement code to clear this volume cache
			 */
			BookModel bookClear = novelCol.getBookCollections().get(groupPosition);
			Toast.makeText(getSherlockActivity(), String.format(getResources().getString(R.string.toast_clear_volume), bookClear.getTitle()), Toast.LENGTH_SHORT).show();
			dao.deleteBookCache(bookClear);
			bookModelAdapter.notifyDataSetChanged();
			return true;
		case R.id.mark_volume:

			/*
			 * Implement code to mark entire volume as read
			 */
			Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_mark_volume), Toast.LENGTH_SHORT).show();
			BookModel book2 = novelCol.getBookCollections().get(groupPosition);
			for (Iterator<PageModel> iPage = book2.getChapterCollection().iterator(); iPage.hasNext();) {
				PageModel page = iPage.next();
				page.setFinishedRead(true);
				dao.updatePageModel(page);
			}
			bookModelAdapter.notifyDataSetChanged();
			return true;
		case R.id.mark_volume2:

			/*
			 * Implement code to mark entire volume as unread
			 */
			Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_mark_volume2), Toast.LENGTH_SHORT).show();
			BookModel ubook2 = novelCol.getBookCollections().get(groupPosition);
			for (Iterator<PageModel> iPage = ubook2.getChapterCollection().iterator(); iPage.hasNext();) {
				PageModel page = iPage.next();
				page.setFinishedRead(false);
				dao.updatePageModel(page);
			}
			bookModelAdapter.notifyDataSetChanged();
			return true;
			// Chapter cases
		case R.id.download_chapter:

			/*
			 * Implement code to download this chapter
			 */
			chapter = bookModelAdapter.getGroup(groupPosition);
			String bookName = novelCol.getBookCollections().get(groupPosition).getTitle();
			touchedForDownload = bookName + " " + chapter.getTitle();
			downloadTask = new DownloadNovelContentTask(chapter, this);
			downloadTask.execute();
			return true;
		case R.id.delete_volume:

			/*
			 * Implement code to delete this volume cache
			 */
			BookModel bookDel = novelCol.getBookCollections().get(groupPosition);
			Toast.makeText(getSherlockActivity(), getResources().getString(R.string.delete_this_volume, bookDel.getTitle()), Toast.LENGTH_SHORT).show();
			dao.deleteBooks(bookDel);
			novelCol.getBookCollections().remove(groupPosition);
			bookModelAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@SuppressLint("NewApi")
	private void executeTask(PageModel pageModel, boolean willRefresh) {
		task = new LoadNovelDetailsTask(willRefresh, this);
		String key = TAG + ":" + pageModel.getPage();
		boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);
		if (isAdded) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new PageModel[] { pageModel });
			else
				task.execute(new PageModel[] { pageModel });
		} else {
			Log.i(TAG, "Continue execute task: " + key);
			LoadNovelDetailsTask tempTask = (LoadNovelDetailsTask) LNReaderApplication.getInstance().getTask(key);
			if (tempTask != null) {
				task = tempTask;
				task.owner = this;
			}
			toggleProgressBar(true);
		}
	}

	@SuppressLint("NewApi")
	private void executeDownloadTask(BookModel chapters, boolean isAll) {
	    if (page != null) {
            downloadTask = new DownloadNovelContentTask(chapters, this);
            String key = TAG + Constants.KEY_DOWNLOAD_CHAPTER + page.getPage();
            if (isAll) {
                key = TAG + Constants.KEY_DOWNLOAD_ALL_CHAPTER + page.getPage();
            }
            boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);

            if (isAdded) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    downloadTask.execute();
            } else {
                Log.i(TAG, "Continue download task: " + key);
                DownloadNovelContentTask tempTask = (DownloadNovelContentTask) LNReaderApplication.getInstance().getTask(key);
                if (tempTask != null) {
                    downloadTask = tempTask;
                    downloadTask.owner = this;
                }
            }
        }
	}

	public boolean downloadListSetup(String id, String toastText, int type, boolean hasError) {
		boolean exists = false;
		if (!this.isAdded() || this.isDetached())
			return exists;

		if (page != null && !Util.isStringNullOrEmpty(page.getTitle())) {
			String name = page.getTitle() + " " + touchedForDownload;
			if (type == 0) {
				if (LNReaderApplication.getInstance().checkIfDownloadExists(name)) {
					exists = true;
					Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_download_on_queue), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_downloading, name), Toast.LENGTH_SHORT).show();
					LNReaderApplication.getInstance().addDownload(id, name);
				}
			} else if (type == 1) {
				Toast.makeText(getSherlockActivity(), "" + toastText, Toast.LENGTH_SHORT).show();
			} else if (type == 2) {
				String downloadDescription = LNReaderApplication.getInstance().getDownloadDescription(id);
				if (downloadDescription != null) {
					String message = getResources().getString(R.string.toast_download_finish, page.getTitle(), downloadDescription);
					if (hasError)
						message = getResources().getString(R.string.toast_download_finish_with_error, page.getTitle(), downloadDescription);
					Toast.makeText(getSherlockActivity(), message, Toast.LENGTH_SHORT).show();
				}
				LNReaderApplication.getInstance().removeDownload(id);
			}
		}
		return exists;
	}

	public void updateProgress(String id, int current, int total, String message) {
		double cur = current;
		double tot = total;
		double result = (cur / tot) * 100;
		LNReaderApplication.getInstance().updateDownload(id, (int) result, message);
		if (loadingBar != null && loadingBar.getVisibility() == View.VISIBLE) {
			loadingBar.setIndeterminate(false);
			loadingBar.setMax(total);
			loadingBar.setProgress(current);
			loadingBar.setProgress(0);
			loadingBar.setProgress(current);
			loadingBar.setMax(total);
		}
	}

	public void toggleProgressBar(boolean show) {
		if (show) {
			loadingText.setText("Loading List, please wait...");
			loadingText.setVisibility(TextView.VISIBLE);
			loadingBar.setVisibility(ProgressBar.VISIBLE);
			expandList.setVisibility(ListView.GONE);
		} else {
			loadingText.setVisibility(TextView.GONE);
			loadingBar.setVisibility(ProgressBar.GONE);
			expandList.setVisibility(ListView.VISIBLE);
		}
	}

	public void setMessageDialog(ICallbackEventData message) {
		if (loadingText.getVisibility() == View.VISIBLE) {
			loadingText.setText(message.getMessage());
		}
	}

	@SuppressLint("NewApi")
	public void onGetResult(AsyncTaskResult<?> result, Class<?> t) {
		if (!isAdded())
			return;

		Exception e = result.getError();
		if (e == null) {
			// from DownloadNovelContentTask
			if (t == NovelContentModel[].class) {
			    NovelContentModel content = (NovelContentModel) result.getResult();
                if (content != null) {
                    Intent intent = new Intent(this.getContext(),BookReadActivity.class);
                    intent.putExtra("net", 1);
                    intent.putExtra("net_content", content.getContent());
                    intent.putExtra("net_book_id", content.getId());
                    startActivity(intent);
                    bookModelAdapter.notifyDataSetChanged();
                }
			}
			// from LoadNovelDetailsTask
			else if (t == NovelCollectionModel.class) {
				novelCol = (NovelCollectionModel) result.getResult();
				// now add the volume and chapter list.
				try {
					// Prepare header
					if ((expandList.getHeaderViewsCount() == 0) && getArguments().getBoolean("show_list_child")) {
						page = novelCol.getPageModel();
						LayoutInflater layoutInflater = getSherlockActivity().getLayoutInflater();
						View synopsis = layoutInflater.inflate(R.layout.activity_display_synopsis, null);
						TextView textViewTitle = (TextView) synopsis.findViewById(R.id.title);
						TextView textViewSynopsis = (TextView) synopsis.findViewById(R.id.synopsys);
						textViewTitle.setTextSize(20);
						textViewSynopsis.setTextSize(16);
						String title = page.getTitle();
						if (page.isTeaser()) {
							title += " (Teaser Project)";
						}
						if (page.isStalled()) {
							title += "\nStatus: Project Stalled";
						}
						if (page.isAbandoned()) {
							title += "\nStatus: Project Abandoned";
						}
						if (page.isPending()) {
							title += "\nStatus: Project Pending Authorization";
						}

						textViewTitle.setText(title);
						textViewSynopsis.setText(novelCol.getSynopsis());

						CheckBox isWatched = (CheckBox) synopsis.findViewById(R.id.isWatched);
						isWatched.setChecked(page.isWatched());
						isWatched.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								if (isChecked) {
									Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_add_watch, page.getTitle()), Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_remove_watch, page.getTitle()), Toast.LENGTH_SHORT).show();
								}
								// update the db!
								page.setWatched(isChecked);
								NovelsDao dao = NovelsDao.getInstance(getSherlockActivity());
								dao.updatePageModel(page);
							}
						});

						ImageView ImageViewCover = (ImageView) synopsis.findViewById(R.id.cover);
						if (novelCol.getCoverBitmap() == null) {
							// IN app test, is returning empty bitmap
							Toast.makeText(getSherlockActivity(), getResources().getString(R.string.toast_err_bitmap_empty), Toast.LENGTH_LONG).show();
						} else {
							if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && UIHelper.getStrechCoverPreference(getSherlockActivity())) {
								Drawable coverDrawable = new BitmapDrawable(getResources(), novelCol.getCoverBitmap());
								int coverHeight = novelCol.getCoverBitmap().getHeight();
								int coverWidth = novelCol.getCoverBitmap().getWidth();
								int screenWidth = (int) (UIHelper.getScreenHeight(getSherlockActivity()) * 0.9);
								int finalHeight = coverHeight * (screenWidth / coverWidth);
								ImageViewCover.setBackground(coverDrawable);
								ImageViewCover.getLayoutParams().height = finalHeight;
								ImageViewCover.getLayoutParams().width = screenWidth;
							} else {
								ImageViewCover.setImageBitmap(novelCol.getCoverBitmap());
								ImageViewCover.getLayoutParams().height = novelCol.getCoverBitmap().getHeight();
								ImageViewCover.getLayoutParams().width = novelCol.getCoverBitmap().getWidth();
							}
						}

						expandList.addHeaderView(synopsis);
					}
					bookModelAdapter = new BookModelAdapter(getSherlockActivity(), novelCol.getBookCollections());
					expandList.setAdapter(bookModelAdapter);
				} catch (Exception e2) {
					Log.e(TAG, "Error when setting up chapter list: " + e2.getMessage(), e2);
					Toast.makeText(getSherlockActivity(), getResources().getString(R.string.error_setting_chapter_list, e2.getMessage()), Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			String message = e.getClass().toString();
			if (e.getMessage() != null)
				message += ": " + e.getMessage();
			Log.e(TAG, message, e);
			Toast.makeText(getSherlockActivity(), message, Toast.LENGTH_SHORT).show();
		}
		toggleProgressBar(false);
	}


	public Context getContext() {
		Context ctx = this.getSherlockActivity();
		if (ctx == null)
			return LNReaderApplication.getInstance().getApplicationContext();
		return ctx;
	}
}
