package com.jens.automation2;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Calendar;

public class News
{
    String headline;
    String text;
    Calendar publishDate;
    String applicablePlattform;

    public static ArrayList<News> downloadNews()
    {
        String newsUrl = "https://server47.de/automation/appNews.php";
        String newsContent = Miscellaneous.downloadURL(newsUrl, null, null);

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
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(publishDateString));
                    newsEntry.setPublishDate(cal);

                    newsEntry.setText(neEl.getElementsByTagName("applicablePlattforms").item(0).getTextContent());

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
}
