package org.inspirecenter.uclancyprusguide.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.inspirecenter.uclancyprusguide.R;

/**
 * @author Nearchos
 *         Created: 03-Jun-16
 */
public class FragmentGallery extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_gallery, null);
        final WebView webView = (WebView) view.findViewById(R.id.fragment_gallery_web_view);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl("http://www.instagram.com/uclancyprus");
        return view;
    }
}
