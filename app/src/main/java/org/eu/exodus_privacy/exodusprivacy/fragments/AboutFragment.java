package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    private final String privacyPolicyURL = "https://exodus-privacy.eu.org/en/page/privacy-policy/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        Spannable privacyPolicy = (Spannable) HtmlCompat.fromHtml(
                getString(R.string.privacyPolicy, privacyPolicyURL),
                HtmlCompat.FROM_HTML_MODE_COMPACT
        );
        for (URLSpan urlSpan : privacyPolicy.getSpans(0, privacyPolicy.length(), URLSpan.class)) {
            privacyPolicy.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint textPaint) {
                    textPaint.setUnderlineText(false);
                }
            }, privacyPolicy.getSpanStart(urlSpan), privacyPolicy.getSpanEnd(urlSpan), 0);
        }
        binding.privacyTV.setText(privacyPolicy);
        binding.privacyTV.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_filter_options).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
