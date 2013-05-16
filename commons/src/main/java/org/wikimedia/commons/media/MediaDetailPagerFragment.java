package org.wikimedia.commons.media;

import android.app.DownloadManager;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import org.wikimedia.commons.*;
import org.wikimedia.commons.contributions.Contribution;
import org.wikimedia.commons.contributions.ContributionsActivity;

public class MediaDetailPagerFragment extends SherlockFragment implements ViewPager.OnPageChangeListener {
    private ViewPager pager;
    private Boolean editable;
    private CommonsApplication app;

    public void onPageScrolled(int i, float v, int i2) {
        getSherlockActivity().supportInvalidateOptionsMenu();
    }

    public void onPageSelected(int i) {
    }

    public void onPageScrollStateChanged(int i) {

    }

    public interface MediaDetailProvider {
        public Media getMediaAtPosition(int i);
        public int getTotalMediaCount();
        public void notifyDatasetChanged();
    }
    private class MediaDetailAdapter extends FragmentStatePagerAdapter {

        public MediaDetailAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if(i == 0) {
                // See bug https://code.google.com/p/android/issues/detail?id=27526
                pager.postDelayed(new Runnable() {
                    public void run() {
                        getSherlockActivity().supportInvalidateOptionsMenu();
                    }
                }, 5);
            }
            return MediaDetailFragment.forMedia(i, editable);
        }

        @Override
        public int getCount() {
            return ((MediaDetailProvider)getActivity()).getTotalMediaCount();
        }
    }

    public MediaDetailPagerFragment() {
        this(false);
    }

    public MediaDetailPagerFragment(Boolean editable) {
        this.editable = editable;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_detail_pager, container, false);
        pager = (ViewPager) view.findViewById(R.id.mediaDetailsPager);
        pager.setOnPageChangeListener(this);
        pager.setAdapter(new MediaDetailAdapter(getChildFragmentManager()));
        if(savedInstanceState != null) {
            final int pageNumber = savedInstanceState.getInt("current-page");
            // Adapter doesn't seem to be loading immediately.
            // Dear God, please forgive us for our sins
            view.postDelayed(new Runnable() {
                public void run() {
                    pager.setCurrentItem(pageNumber, false);
                    getSherlockActivity().supportInvalidateOptionsMenu();
                }
            }, 100);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current-page", pager.getCurrentItem());
        outState.putBoolean("editable", editable);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
        }
        app = (CommonsApplication)getActivity().getApplicationContext();
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MediaDetailProvider provider = (MediaDetailProvider)getSherlockActivity();
        Media m = provider.getMediaAtPosition(pager.getCurrentItem());
        switch(item.getItemId()) {
            case R.id.menu_share_current_image:
                EventLog.schema(CommonsApplication.EVENT_SHARE_ATTEMPT)
                        .param("username", app.getCurrentAccount().name)
                        .param("filename", m.getFilename())
                        .log();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, m.getDisplayTitle() + " " + m.getDescriptionUrl());
                startActivity(shareIntent);
                return true;
            case R.id.menu_browser_current_image:
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(m.getDescriptionUrl()));
                startActivity(viewIntent);
                return true;
            case R.id.menu_download_current_image:
                downloadMedia(m);
                return true;
            case R.id.menu_retry_current_image:
                // Is this... sane? :)
                ((ContributionsActivity)getSherlockActivity()).retryUpload(pager.getCurrentItem());
                getSherlockActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.menu_abort_current_image:
                // todo: delete image
                ((ContributionsActivity)getSherlockActivity()).deleteUpload(pager.getCurrentItem());
                getSherlockActivity().getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Start the media file downloading to the local SD card/storage.
     * The file can then be opened in Gallery or other apps.
     *
     * @param m
     */
    private void downloadMedia(Media m) {
        String imageUrl = m.getImageUrl(),
               fileName = m.getFilename();
        Uri imageUri = Uri.parse(imageUrl);

        DownloadManager.Request req = new DownloadManager.Request(imageUri);
        req.setDescription(m.getDisplayTitle());
        req.setTitle(m.getDisplayTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            req.allowScanningByMediaScanner();
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager)getSherlockActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(req);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!editable) { // Disable menu options for editable views
            menu.clear(); // see http://stackoverflow.com/a/8495697/17865
            inflater.inflate(R.menu.fragment_image_detail, menu);
            if(pager != null) {
                MediaDetailProvider provider = (MediaDetailProvider)getSherlockActivity();
                Media m = provider.getMediaAtPosition(pager.getCurrentItem());
                if(m != null && !m.getFilename().startsWith("File:")) {
                    // Crude way of checking if the file has been successfully saved!
                    menu.findItem(R.id.menu_browser_current_image).setEnabled(false).setVisible(false);
                    menu.findItem(R.id.menu_share_current_image).setEnabled(false).setVisible(false);
                    menu.findItem(R.id.menu_retry_current_image).setEnabled(true).setVisible(true);
                    menu.findItem(R.id.menu_abort_current_image).setEnabled(true).setVisible(true);
                    return;
                }
            }
        }
    }

    public void showImage(int i) {
        pager.setCurrentItem(i);
    }
}