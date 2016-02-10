package com.newsapps.newstamil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import me.relex.circleindicator.CircleIndicator;

public class ArticleImagesPagerActivity extends CommonActivity implements GestureDetector.OnDoubleTapListener {
    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<String> m_urls;
    private ArrayList<String> m_checkedUrls;
    private String m_title;
    private ArticleImagesPagerAdapter m_adapter;
    private String m_content;
    private GestureDetector m_detector;
    private ProgressBar m_progress;
    private ViewPager m_pager;

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    private class ArticleImagesPagerAdapter extends PagerAdapter {
        private List<String> m_urls;

        public ArticleImagesPagerAdapter(List<String> urls) {
            super();

            m_urls = urls;
        }

        @Override
        public int getCount() {
            return m_urls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final String url = m_urls.get(position);

            //Log.d(TAG, "called for URL: " + url);

            LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.article_images_image, null);

            m_detector = new GestureDetector(ArticleImagesPagerActivity.this, new GestureDetector.SimpleOnGestureListener());

            m_detector.setOnDoubleTapListener(ArticleImagesPagerActivity.this);

            ImageViewTouch imgView = (ImageViewTouch) view.findViewById(R.id.flavor_image);

            imgView.setFitToScreen(true);
            //imgView.setFitToWidth(true);

            imgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    return m_detector.onTouchEvent(event);
                }
            });

            if (position == 0) {
                ViewCompat.setTransitionName(imgView, "gallery:" + url);
            }

            registerForContextMenu(imgView);

            view.findViewById(R.id.flavor_image_overflow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(ArticleImagesPagerActivity.this, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.context_article_content_img, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return onImageMenuItemSelected(item, url);
                        }
                    });

                    popup.show();

                }
            });

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .resetViewBeforeLoading(true)
                    .cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(200))
                    .build();

            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.flavor_image_progress);
            final View errorMessage = view.findViewById(R.id.flavor_image_error);

            ImageLoader.getInstance().displayImage(url, imgView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (bitmap != null) {
                        view.setTag(s);
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });

            container.addView(view, 0);

            if (position == 0) {
                ActivityCompat.startPostponedEnterTransition(ArticleImagesPagerActivity.this);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FrameLayout)object);
        }
    }

    private class ImageCheckTask extends AsyncTask<List<String>, String, Integer> {
        @Override
        protected Integer doInBackground(List<String>... urls) {
            int position = 0;

            for (String url : urls[0]) {
                if (!isCancelled()) {
                    position++;

                    //Log.d(TAG, "checking: " + url);

                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .build();

                    Bitmap bmp = ImageLoader.getInstance().loadImageSync(url, options);

                    if (bmp != null && bmp.getWidth() > 128 && bmp.getHeight() > 128) {
                        publishProgress(url, String.valueOf(position));
                    } else {
                        publishProgress(null, String.valueOf(position));
                    }
                }
            }

            return -1;
        }

        @Override
        protected void onProgressUpdate(String... checkedUrl) {

            if (!isFinishing() && m_adapter != null) {
                if (checkedUrl[0] != null) {
                    m_checkedUrls.add(checkedUrl[0]);
                    m_adapter.notifyDataSetChanged();
                }

                //Log.d(TAG, "progr=" + checkedUrl[1]);

                m_progress.setProgress(Integer.valueOf(checkedUrl[1]));
            } else {
                cancel(true);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            m_progress.setVisibility(View.GONE);

            CircleIndicator indicator = (CircleIndicator) findViewById(R.id.article_images_indicator);

            if (indicator != null) {
                indicator.setViewPager(m_pager);
                indicator.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ActivityCompat.postponeEnterTransition(this);

        // we use that before parent onCreate so let's init locally
        m_prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        setTheme(R.style.DarkTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.article_images_pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_progress = (ProgressBar) findViewById(R.id.article_images_progress);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();

        if (savedInstanceState == null) {
            m_title = getIntent().getStringExtra("title");
            //m_urls = getIntent().getStringArrayListExtra("urls");
            m_content = getIntent().getStringExtra("content");

            String imgSrcFirst = getIntent().getStringExtra("firstSrc");

            m_urls = new ArrayList<String>();

            Document doc = Jsoup.parse(m_content);
            Elements imgs = doc.select("img");

            boolean firstFound = false;

            for (Element img : imgs) {
                String imgSrc = img.attr("src");

                if (imgSrc.startsWith("//")) {
                    imgSrc = "https:" + imgSrc;
                }

                if (imgSrcFirst.equals(imgSrc))
                    firstFound = true;

                if (firstFound) {
                    m_urls.add(imgSrc);
                }
            }

        } else {
            m_urls = savedInstanceState.getStringArrayList("urls");
            m_title = savedInstanceState.getString("title");
            m_content = savedInstanceState.getString("content");
        }

        if (m_urls.size() > 1) {
            m_progress.setProgress(0);
            m_progress.setMax(m_urls.size());
            m_checkedUrls = new ArrayList<String>();

            ArrayList<String> tmp = new ArrayList<String>(m_urls);

            m_checkedUrls.add(tmp.get(0));
            tmp.remove(0);

            ImageCheckTask ict = new ImageCheckTask();
            ict.execute(tmp);
        } else {
            m_checkedUrls = new ArrayList<String>(m_urls);
            m_progress.setVisibility(View.GONE);
        }

        setTitle(m_title);

        m_adapter = new ArticleImagesPagerAdapter(m_checkedUrls);

        m_pager = (ViewPager) findViewById(R.id.article_images_pager);
        m_pager.setAdapter(m_adapter);
        m_pager.setPageTransformer(true, new DepthPageTransformer());

    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        getMenuInflater().inflate(R.menu.context_article_content_img, menu);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        out.putStringArrayList("urls", m_urls);
        out.putString("title", m_title);
        out.putString("content", m_content);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = m_pager.getCurrentItem();
        String url = m_checkedUrls.get(position);

        if (!onImageMenuItemSelected(item, url))
            return super.onContextItemSelected(item);
        else
            return true;
    }

    public boolean onImageMenuItemSelected(MenuItem item, String url) {
        switch (item.getItemId()) {
            case R.id.article_img_open:
                if (url != null) {
                    try {
                        openUri(Uri.parse(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast(R.string.error_other_error);
                    }
                }
                return true;
            case R.id.article_img_copy:
                if (url != null) {
                    copyToClipboard(url);
                }
                return true;
            case R.id.article_img_share:
                if (url != null) {
                    shareText(url);
                }
                return true;
            case R.id.article_img_view_caption:
                if (url != null) {
                    displayImageCaption(url, m_content);
                }
                return true;
            default:
                //Log.d(TAG, "onImageMenuItemSelected, unhandled id=" + item.getItemId());
                return false;
        }
    }

}
