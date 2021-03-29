package com.jens.automation2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class News
{
    Calendar publishDate;
    String applicablePlattform;
    Map<String,NewsTranslation> translations = new HashMap<>();

    public static class NewsTranslation
    {
        String language;
        String headline;
        String text;

        public String getLanguage()
        {
            return language;
        }

        public void setLanguage(String language)
        {
            this.language = language;
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
    }

    public static ArrayList<News> downloadNews(Context context)
    {
        Calendar now = Calendar.getInstance();
        String newsContent;

        String newsFileName = "appNews.xml";

        String filePath = context.getCacheDir() + "/" + newsFileName;

        File oldFilePath = new File(context.getFilesDir() + "/" + newsFileName);
        if(oldFilePath.exists())
            oldFilePath.delete();

        if (!(new File(filePath)).exists() || Settings.lastNewsPolltime == -1 || now.getTimeInMillis() >= Settings.lastNewsPolltime + (long)(Settings.newsDisplayForXDays * 24 * 60 * 60 * 1000))
        {
            String newsUrl = "https://server47.de/automation/appNews.php";
            newsContent = Miscellaneous.downloadURL(newsUrl, null, null);

            // Cache content to local storage
            if(Miscellaneous.writeStringToFile(filePath, newsContent))
            {
                Settings.lastNewsPolltime = now.getTimeInMillis();
                Settings.writeSettings(context);
                Miscellaneous.logEvent("i", newsFileName, "File stored to " + filePath, 5);
            }
        }
        else
        {
            // Just read local cache file
            newsContent = Miscellaneous.readFileToString(filePath);
            Miscellaneous.logEvent("i", newsFileName, "Using cache to retrieve news: " + filePath, 5);
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

                    NodeList headLineNodes = neEl.getElementsByTagName("headline");
                    for(int h = 0; h <  headLineNodes.getLength(); h++)
                    {
                        NamedNodeMap attrMap = headLineNodes.item(h).getAttributes();
                        for(int n = 0; n < attrMap.getLength(); n++)
                        {
                            if(attrMap.item(n).getNodeName().equalsIgnoreCase("language"))
                            {
                                String language = attrMap.item(n).getTextContent();
                                if(!newsEntry.getTranslations().containsKey(language))
                                    newsEntry.getTranslations().put(language, new NewsTranslation());

                                newsEntry.getTranslations().get(language).setHeadline(headLineNodes.item(h).getTextContent());
                            }
                        }
                    }

                    NodeList textNodes = neEl.getElementsByTagName("text");
                    for(int t = 0; t <  textNodes.getLength(); t++)
                    {
                        NamedNodeMap attrMap = textNodes.item(t).getAttributes();
                        for(int n = 0; n < attrMap.getLength(); n++)
                        {
                            if(attrMap.item(n).getNodeName().equalsIgnoreCase("language"))
                            {
                                String language = attrMap.item(n).getTextContent();
                                if(!newsEntry.getTranslations().containsKey(language))
                                    newsEntry.getTranslations().put(language, new NewsTranslation());

                                newsEntry.getTranslations().get(language).setText(textNodes.item(t).getTextContent());
                            }
                        }
                    }

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

    public Map<String, NewsTranslation> getTranslations()
    {
        return translations;
    }

    public void setTranslations(Map<String, NewsTranslation> translations)
    {
        this.translations = translations;
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

        String langToChoose = "en";
        String systemLanguage = Locale.getDefault().getLanguage();
        if(this.getTranslations().containsKey(systemLanguage))
            langToChoose = systemLanguage;

        return this.getTranslations().get(langToChoose).getHeadline() + " " + Miscellaneous.getAnyContext().getString(R.string.publishedOn) + " " + timestamp + Miscellaneous.lineSeparator + this.getTranslations().get(langToChoose).getText();
    }

    public String toStringHtml()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");;
        Calendar calendar = Calendar.getInstance();
        Date now = this.getPublishDate().getTime();
        String timestamp = sdf.format(now);

        String langToChoose = "en";
        String systemLanguage = Locale.getDefault().getLanguage();
        if(this.getTranslations().containsKey(systemLanguage))
            langToChoose = systemLanguage;

        return "<b><u><i>" + this.getTranslations().get(langToChoose).getHeadline() + "</i></u></b>" + " " + Miscellaneous.getAnyContext().getString(R.string.publishedOn) + " " + timestamp + "<br>" + this.getTranslations().get(langToChoose).getText();
    }

    public static class AsyncTaskDownloadNews extends AsyncTask<Context, Void, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(Context... contexts)
        {
            try
            {
                Calendar limit = Calendar.getInstance();
                limit.add(Calendar.DAY_OF_MONTH, -Settings.newsPollEveryXDays);
                return downloadNews(contexts[0], limit);
            }
            catch(Exception e)
            {
                Miscellaneous.logEvent("e", "Error displaying news", Log.getStackTraceString(e), 3);
                return new ArrayList();
            }
        }

        @Override
        protected void onPostExecute(ArrayList arrayList)
        {
            try
            {
                ActivityMainScreen.getActivityMainScreenInstance().processNewsResult(arrayList);
            }
            catch(NullPointerException e)
            {
                Miscellaneous.logEvent("e", "NewsDownload", "There was a problem displaying the already downloded news, probably ActivityMainScreen isn't currently shown: " + Log.getStackTraceString(e), 2);
            }
            catch(Exception e)
            {
                Miscellaneous.logEvent("e", "NewsDownload", "There was a problem displaying the already downloded news: " + Log.getStackTraceString(e), 2);
            }
        }
    }
}