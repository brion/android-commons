package org.wikimedia.commons.media;

import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.wikimedia.commons.*;

public class MediaDetailFragment extends SherlockFragment {

    private boolean editable;
    private DisplayImageOptions displayOptions;
    private MediaDetailPagerFragment.MediaDetailProvider detailProvider;
    private int index;

    public static MediaDetailFragment forMedia(int index) {
        return forMedia(index, false);
    }

    public static MediaDetailFragment forMedia(int index, boolean editable) {
        MediaDetailFragment mf = new MediaDetailFragment();

        Bundle state = new Bundle();
        state.putBoolean("editable", editable);
        state.putInt("index", index);

        mf.setArguments(state);

        return mf;
    }

    private ImageView image;
    private EditText title;
    private ProgressBar loadingProgress;
    private ImageView loadingFailed;
    private FrameLayout frameLayout;
    private FrameLayout viewerLayout;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", index);
        outState.putBoolean("editable", editable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        detailProvider = (MediaDetailPagerFragment.MediaDetailProvider)getActivity();

        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
            index = savedInstanceState.getInt("index");
        } else {
            editable = getArguments().getBoolean("editable");
            index = getArguments().getInt("index");
        }
        final Media media = detailProvider.getMediaAtPosition(index);

        View view = inflater.inflate(R.layout.fragment_media_detail, container, false);
        image = (ImageView) view.findViewById(R.id.mediaDetailImage);
        title = (EditText) view.findViewById(R.id.mediaDetailTitle);
        loadingProgress = (ProgressBar) view.findViewById(R.id.mediaDetailImageLoading);
        loadingFailed = (ImageView) view.findViewById(R.id.mediaDetailImageFailed);
        viewerLayout = (FrameLayout) view.findViewById(R.id.mediaDetailViewerLayout);
        frameLayout = (FrameLayout) view.findViewById(R.id.mediaDetailFrameLayout);

        Log.d("Commons", "viewerLayout is " + viewerLayout);
        Log.d("Commons", "container.getHeight() is " + container.getHeight());
        Log.d("Commons", "view.getHeight() is " + view.getHeight());
        Log.d("Commons", "frameLayout.getHeight() is " + frameLayout.getHeight());
        viewerLayout.setMinimumHeight(640);

        // Enable or disable editing on the title
        title.setClickable(editable);
        title.setFocusable(editable);
        title.setCursorVisible(editable);
        title.setFocusableInTouchMode(editable);
        if(!editable) {
            title.setBackgroundDrawable(null);
        }

        String actualUrl = TextUtils.isEmpty(media.getImageUrl()) ? media.getLocalUri().toString() : media.getThumbnailUrl(640);
        ImageLoader.getInstance().displayImage(actualUrl, image, displayOptions, new ImageLoadingListener() {
            public void onLoadingStarted(String s, View view) {
                loadingProgress.setVisibility(View.VISIBLE);
            }

            public void onLoadingFailed(String s, View view, FailReason failReason) {
                loadingProgress.setVisibility(View.GONE);
                loadingFailed.setVisibility(View.VISIBLE);
            }

            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                loadingProgress.setVisibility(View.GONE);
                loadingFailed.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                if(bitmap.hasAlpha()) {
                    image.setBackgroundResource(android.R.color.white);
                }
            }

            public void onLoadingCancelled(String s, View view) {
                throw new RuntimeException("Image loading cancelled. But why?");
            }
        });
        title.setText(media.getDisplayTitle());

        title.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                detailProvider.getMediaAtPosition(index).setFilename(title.getText().toString());
                detailProvider.getMediaAtPosition(index).setTag("isDirty", true);
                detailProvider.notifyDatasetChanged();
            }

            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        displayOptions = Utils.getGenericDisplayOptions().build();
    }
}
