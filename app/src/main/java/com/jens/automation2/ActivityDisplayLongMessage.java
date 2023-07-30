package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

public class ActivityDisplayLongMessage extends Activity
{
    TextView tvMessageTitle, tvLongMessage, tvMessageLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_display_long_message);

        tvMessageTitle = (TextView)findViewById(R.id.tvMessageTitle);
        tvLongMessage = (TextView)findViewById(R.id.tvLongMessage);
        tvMessageLink = (TextView)findViewById(R.id.tvMessageLink);

        String title = getIntent().getStringExtra("messageTitle");
        String message = getIntent().getStringExtra("longMessage").replace("\\n", Miscellaneous.lineSeparator);

        String link = null;
        if(getIntent().hasExtra("messageLink"))
            link = getIntent().getStringExtra("messageLink");

        tvMessageTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
        tvLongMessage.setText(message);

        if(link != null && link.length() > 0)
        {
            tvMessageLink.setText(HtmlCompat.fromHtml("<u>" + link + "</u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            String finalLink = link;
            tvMessageLink.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Uri uriUrl = Uri.parse(finalLink);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });
        }
    }
}