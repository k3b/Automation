package com.jens.automation2;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class News
{
    String headline;
    String text;
    Calendar publishDate;
    String applicablePlattform;

    public static ArrayList<News> downloadNews(Context context)
    {
        Calendar now = Calendar.getInstance();
        String newsContent;

        if (Settings.lastNewsPolltime == -1 || now.getTimeInMillis() >= Settings.lastNewsPolltime + (long)(Settings.pollNewsEveryXDays * 24 * 60 * 60 * 1000))
        {
            String newsUrl = "https://server47.de/automation/appNews.php";
            newsContent = Miscellaneous.downloadURL(newsUrl, null, null);

            // Cache content to local storage
            if(Miscellaneous.writeStringToFile(context.getFilesDir() + "/appNews.xml", newsContent))
            {
                Settings.lastNewsPolltime = now.getTimeInMillis();
                Settings.writeSettings(context);
            }
        }
        else
        {
            // Just read local cache file
            newsContent = Miscellaneous.readFileToString(context.getFilesDir() + "/appNews.xml");
        }

        ArrayList<News> returnList = new ArrayList<>();

        try
        {
            Element automationRootElement = Miscellaneous.getXmlTree(newsContent);

            NodeList newsEntriesElements = automationRootElement.getElementsByTagName("newsEntries");

            NodeList newsEntryElements = ((Element)newsEntriesElements.item(0)).getElementsByTagName("newsEntry");

            for (int i = 0; i < newsEntryElements.getLength(); i++)
            {
                if (newsEntryElements.item(i).getNodeType() == Node.ELEMENT_NODE && (newsEntryElements.item(i).getParentNode().isSameNode(((Element)newsEntriesElements.item(0)))))
                {
                    News newsEntry = new News();

                    Element neEl = (Element)newsEntryElements.item(i);
                    newsEntry.setHeadline(neEl.getElementsByTagName("headline").item(0).getTextContent());
                    newsEntry.setText(neEl.getElementsByTagName("text").item(0).getTextContent());

                    String publishDateString = neEl.getElementsByTagName("publishDate").item(0).getTextContent();
                    newsEntry.setPublishDate(Miscellaneous.calendarFromLong(Long.parseLong(publishDateString) * 1000));

                    newsEntry.setApplicablePlattform(neEl.getElementsByTagName("applicablePlattforms").item(0).getTextContent());

                    if(newsEntry.getApplicablePlattform().equalsIgnoreCase("all") || newsEntry.getApplicablePlattform().equalsIgnoreCase(BuildConfig.FLAVOR))
                        returnList.add(newsEntry);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return returnList;
    }

    public static ArrayList<News> downloadNews(Context context, Calendar ageLimit)
    {
        ArrayList<News> returnList = new ArrayList<>();

        for(News newsEntry : downloadNews(context))
        {
            if (newsEntry.getPublishDate().getTimeInMillis() >= ageLimit.getTimeInMillis())
                returnList.add(newsEntry);
        }

        return returnList;
    }

    public String getHeadline()
    {
        return headline;
    }

    public void setHeadline(String headline)
    {
        this.headline = headline;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public Calendar getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(Calendar publishDate)
    {
        this.publishDate = publishDate;
    }

    public String getApplicablePlattform()
    {
        return applicablePlattform;
    }

    public void setApplicablePlattform(String applicablePlattform)
    {
        this.applicablePlattform = applicablePlattform;
    }

    @NonNull
    @Override
    public String toString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");;
        Calendar calendar = Calendar.getInstance();
        Date now = this.getPublishDate().getTime();
        String timestamp = sdf.format(now);

        return this.getHeadline() + " published on " + timestamp + Miscellaneous.lineSeparator + this.getText();
    }

    public String toStringHtml()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");;
        Calendar calendar = Calendar.getInstance();
        Date now = this.getPublishDate().getTime();
        String timestamp = sdf.format(now);

        return "<b><u><i>" + this.getHeadline() + "</i></u></b>" + " published on " + timestamp + "<br>" + this.getText();
    }

    public static class AsyncTaskDownloadNews extends AsyncTask<Context, Void, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(Context... contexts)
        {
            return downloadNews(contexts[0]);
        }

        @Override
        protected void onPostExecute(ArrayList arrayList)
        {
            ActivityMainScreen.getActivityMainScreenInstance().processNewsResult(arrayList);
        }
    }
}
