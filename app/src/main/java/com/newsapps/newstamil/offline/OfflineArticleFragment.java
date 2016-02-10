package com.newsapps.newstamil.offline;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.newsapps.newstamil.R;
import com.newsapps.newstamil.util.ImageCacheService;
import com.newsapps.newstamil.util.NotifyingScrollView;
import com.shamanland.fab.ShowHideOnScroll;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OfflineArticleFragment extends Fragment {
	private final String TAG = this.getClass().getSimpleName();

	private SharedPreferences m_prefs;
	private int m_articleId;
	private boolean m_isCat = false; // FIXME use
	private Cursor m_cursor;
	private OfflineActivity m_activity;

	@Nullable
	private AdView mAdView;

	public void initialize(int articleId) {
		m_articleId = articleId;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		/* AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo(); */
		
		switch (item.getItemId()) {
		case R.id.article_link_share:
			m_activity.shareArticle(m_articleId);
			return true;
		case R.id.article_link_copy:
			if (true) {
				Cursor article = m_activity.getArticleById(m_articleId);
				
				if (article != null) {				
					m_activity.copyToClipboard(article.getString(article.getColumnIndex("link")));
					article.close();
				}
			}
			return true;
		default:
			//Log.d(TAG, "onContextItemSelected, unhandled id=" + item.getItemId());
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		
		//getActivity().getMenuInflater().inflate(R.menu.context_article_link, menu);
		//menu.setHeaderTitle(m_cursor.getString(m_cursor.getColumnIndex("title")));
		
		String title = m_cursor.getString(m_cursor.getColumnIndex("title"));
		
		if (v.getId() == R.id.article_content) {
			HitTestResult result = ((WebView)v).getHitTestResult();

			if (result != null && (result.getType() == HitTestResult.IMAGE_TYPE || result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
				menu.setHeaderTitle(result.getExtra());
				getActivity().getMenuInflater().inflate(R.menu.context_article_content_img, menu);
				
				/* FIXME I have no idea how to do this correctly ;( */
				
				m_activity.setLastContentImageHitTestUrl(result.getExtra());
				
			} else {
				menu.setHeaderTitle(title);
				getActivity().getMenuInflater().inflate(R.menu.context_article_link, menu);
			}
		} else {
			menu.setHeaderTitle(title);
			getActivity().getMenuInflater().inflate(R.menu.context_article_link, menu);
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);	
		
	}
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    	

		if (savedInstanceState != null) {
			m_articleId = savedInstanceState.getInt("articleId");
		}
		
		View view = inflater.inflate(R.layout.fragment_article, container, false);

		m_cursor = m_activity.getDatabase().query("articles LEFT JOIN feeds ON (feed_id = feeds."+BaseColumns._ID+")", 
				new String[] { "articles.*", "feeds.title AS feed_title" }, "articles." + BaseColumns._ID + "=?", 
				new String[] { String.valueOf(m_articleId) }, null, null, null);

		m_cursor.moveToFirst();
		
		if (m_cursor.isFirst()) {
            final String link = m_cursor.getString(m_cursor.getColumnIndex("link"));

            NotifyingScrollView scrollView = (NotifyingScrollView) view.findViewById(R.id.article_scrollview);
            View fab = view.findViewById(R.id.article_fab);

            if (scrollView != null && m_activity.isSmallScreen()) {
                view.findViewById(R.id.article_heading_spacer).setVisibility(View.VISIBLE);

                scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                        ActionBar ab = m_activity.getSupportActionBar();

                        if (t >= oldt && t >= ab.getHeight()) {
                            ab.hide();
                        } else if (t <= ab.getHeight() || oldt - t >= 10) {
                            ab.show();
                        }

                    }
                });
            }

            if (scrollView != null && fab != null) {
                if (m_prefs.getBoolean("enable_article_fab", true)) {
                    scrollView.setOnTouchListener(new ShowHideOnScroll(fab));

                    fab.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                URL url = new URL(link.trim());
                                String uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                                        url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toString();

								m_activity.openUri(Uri.parse(uri));
                            } catch (Exception e) {
                                e.printStackTrace();
                                m_activity.toast(R.string.error_other_error);
                            }
                        }
                    });
                } else {
                    fab.setVisibility(View.GONE);
                }
            }

			int articleFontSize = Integer.parseInt(m_prefs.getString("article_font_size_sp", "16"));
			int articleSmallFontSize = Math.max(10, Math.min(18, articleFontSize - 2));
			
			TextView title = (TextView)view.findViewById(R.id.title);

			if (title != null) {

                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.min(21, articleFontSize + 3));

				String titleStr;
				
				if (m_cursor.getString(m_cursor.getColumnIndex("title")).length() > 200)
					titleStr = m_cursor.getString(m_cursor.getColumnIndex("title")).substring(0, 200) + "...";
				else
					titleStr = m_cursor.getString(m_cursor.getColumnIndex("title"));
								
				title.setText(titleStr);
				//title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				title.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							m_activity.openUri(Uri.parse(link));
						} catch (Exception e) {
							e.printStackTrace();
							m_activity.toast(R.string.error_other_error);
						}
					}
				});

			}

			ImageView share = (ImageView)view.findViewById(R.id.share);

			if (share != null) {
				share.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						m_activity.shareArticle(m_articleId);
					}
				});
			}


			TextView comments = (TextView)view.findViewById(R.id.comments);
			
			if (comments != null) {
				comments.setVisibility(View.GONE);
			}
			
			TextView note = (TextView)view.findViewById(R.id.note);
			
			if (note != null) {
				note.setVisibility(View.GONE);
			}
			
			final WebView web = (WebView)view.findViewById(R.id.article_content);
			
			if (web != null) {

				web.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						try {
							m_activity.openUri(Uri.parse(url));

							return true;

						} catch (Exception e){
							e.printStackTrace();
						}

						return false;
					} });

				web.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        HitTestResult result = ((WebView) v).getHitTestResult();

                        if (result != null && (result.getType() == HitTestResult.IMAGE_TYPE || result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
                            registerForContextMenu(web);
                            m_activity.openContextMenu(web);
                            unregisterForContextMenu(web);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

				mAdView = (AdView) view.findViewById(R.id.adView);
				setupAds(mAdView);

                // prevent flicker in ics
                if (!m_prefs.getBoolean("webview_hardware_accel", true)) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                }

                String content;
                String cssOverride = "";

                WebSettings ws = web.getSettings();
                ws.setSupportZoom(false);

				// we need to show "insecure" file:// urls
				if (m_prefs.getBoolean("offline_image_cache_enabled", false)) {
					ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
				}

                TypedValue tvBackground = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.articleBackground, tvBackground, true);

                String backgroundHexColor = String.format("#%06X", (0xFFFFFF & tvBackground.data));

                cssOverride = "body { background : "+ backgroundHexColor+"; }";

                TypedValue tvTextColor = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.articleTextColor, tvTextColor, true);

                String textColor = String.format("#%06X", (0xFFFFFF & tvTextColor.data));

                cssOverride += "body { color : "+textColor+"; }";

                TypedValue tvLinkColor = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.linkColor, tvLinkColor, true);

                String linkHexColor = String.format("#%06X", (0xFFFFFF & tvLinkColor.data));
                cssOverride += " a:link {color: "+linkHexColor+";} a:visited { color: "+linkHexColor+";}";

				String articleContent = m_cursor.getString(m_cursor.getColumnIndex("content"));
				Document doc = Jsoup.parse(articleContent);
					
				if (doc != null) {
					if (m_prefs.getBoolean("offline_image_cache_enabled", false)) {
						
						Elements images = doc.select("img");
						
						for (Element img : images) {
							String url = img.attr("src");
							
							if (ImageCacheService.isUrlCached(m_activity, url)) {						
								img.attr("src", "file://" + ImageCacheService.getCacheFileName(m_activity, url));
							}						
						}
					}
					
					// thanks webview for crashing on <video> tag
					Elements videos = doc.select("video");
					
					for (Element video : videos)
						video.remove();
					
					articleContent = doc.toString();
				}
				
				if (m_prefs.getBoolean("justify_article_text", true)) {
					cssOverride += "body { text-align : justify; } ";
				}
				
				ws.setDefaultFontSize(articleFontSize);

                content =
                    "<html>" +
                    "<head>" +
                    "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />" +
                    "<style type=\"text/css\">" +
                    "body { padding : 0px; margin : 0px; line-height : 130%; }" +
                    "img { max-width : 100%; width : auto; height : auto; }" +
                    " table { width : 100%; }" +
                    cssOverride +
                    "</style>" +
                    "</head>" +
                    "<body>" + articleContent;
				
				content += "</body></html>";
				
				try {
					String baseUrl = null;
					
					try {
						URL url = new URL(link);
						baseUrl = url.getProtocol() + "://" + url.getHost();
					} catch (MalformedURLException e) {
						//
					}
					
					web.loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null);
				} catch (RuntimeException e) {					
					e.printStackTrace();
				}
				
			
			}
			
			TextView dv = (TextView)view.findViewById(R.id.date);
			
			if (dv != null) {
				dv.setTextSize(TypedValue.COMPLEX_UNIT_SP, articleSmallFontSize);
				
				Date d = new Date(m_cursor.getInt(m_cursor.getColumnIndex("updated")) * 1000L);
				DateFormat df = new SimpleDateFormat("MMM dd, HH:mm");
				dv.setText(df.format(d));
			}

			TextView tagv = (TextView)view.findViewById(R.id.tags);
						
			if (tagv != null) {
				tagv.setTextSize(TypedValue.COMPLEX_UNIT_SP, articleSmallFontSize);

				int feedTitleIndex = m_cursor.getColumnIndex("feed_title");

				if (feedTitleIndex != -1 /* && m_isCat */) {
					String fTitle = m_cursor.getString(feedTitleIndex);
					
					int authorIndex = m_cursor.getColumnIndex("author");
					
					if (authorIndex >= 0) {
						String authorStr = m_cursor.getString(authorIndex);

						if (authorStr != null && authorStr.length() > 0) {
							fTitle += " (" + getString(R.string.author_formatted, m_cursor.getString(authorIndex)) + ")";
						}
					}
					
					tagv.setText(fTitle);
				} else {				
					String tagsStr = m_cursor.getString(m_cursor.getColumnIndex("tags"));
					tagv.setText(tagsStr);
				}
			}	
			
		} 
		
		return view;    	
	}

	private void setupAds(AdView mAdView) {
		if(mAdView != null) {
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("6E07B8FFD49809372F3C4498948BD73B")
					.addTestDevice("A5180AEB9D609E83B29B3D9FA56400D8")
					.build();
			mAdView.loadAd(adRequest);
		}
	}

	@Override
	public void onPause() {
		if (mAdView != null) {
			mAdView.pause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mAdView != null) {
			mAdView.resume();
		}
	}

	@Override
	public void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();	
		
		m_cursor.close();
	}
	
	@Override
	public void onSaveInstanceState (Bundle out) {		
		super.onSaveInstanceState(out);
		
		out.putInt("articleId", m_articleId);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);		
		
		m_prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		m_activity = (OfflineActivity) activity;
		
	}
}