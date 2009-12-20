package com.atlassian.uwc.ui.organizer;

import java.util.ArrayList;
import java.util.List;

public class WikiPage {

    private int pageID;

    private int parentID;

    private String pageName;

    private List childPages;

    public WikiPage(int aPageID, int aParentID, String aPageName) {

        pageID = aPageID;
        parentID = aParentID;
        pageName = aPageName;
    }

    public List getChildPages() {
        if (childPages == null) {
            childPages = new ArrayList();
        }

        return childPages;
    }

    public int getPageID() {
        return pageID;
    }

    public int getParentID() {
        return parentID;
    }

    public String getPageName() {
        return pageName;
    }

    public String toString() {
        return getPageName();
    }

    public static List getExampleFlatList() {

        List exampleSite = new ArrayList();

        exampleSite.add(new WikiPage(11, 8, "ArtisanPlugins"));

        exampleSite.add(new WikiPage(4, 1, "ToDoList"));

        exampleSite.add(new WikiPage(8, 7, "WikiPlugins"));
        exampleSite.add(new WikiPage(10, 8, "AtlassianPlugins"));
        exampleSite.add(new WikiPage(12, 8, "ArtemisPlugins"));

        exampleSite.add(new WikiPage(1, 0, "BrettsSpace"));
        exampleSite.add(new WikiPage(2, 1, "Addresses"));
        exampleSite.add(new WikiPage(3, 1, "Calendar"));

        exampleSite.add(new WikiPage(5, 1, "Links"));

        exampleSite.add(new WikiPage(6, 0, "BrettsWorkSpace"));
        exampleSite.add(new WikiPage(7, 6, "WorkProjects"));
        exampleSite.add(new WikiPage(9, 7, "BuildingManagement"));


        exampleSite.add(new WikiPage(13, 0, "NewTestSpace"));

        return exampleSite;
    }
}
