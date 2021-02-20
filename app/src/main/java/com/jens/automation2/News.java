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

    public static ArrayList<News> extractNewsFromString()
    {
        String result =
        Miscellaneous.messageBox(title, text, ActivityMainScreen.getActivityMainScreenInstance());

        Element homeControlRootElement = Miscellaneous.getXmlTree(inventoryString);
        if(homeControlRootElement.getAttribute("protocolVersion").equals(String.valueOf(requiredProtocolVersion)))
        {
            FullDataModel.getInstance().houseList = new ArrayList<HouseTemplate>();
//                FullDataModel.getInstance().roomList = new ArrayList<RoomTemplate>();
            FullDataModel.getInstance().nodeList = new ArrayList<NodeTemplate>();
//                FullDataModel.getInstance().deviceList = new ArrayList<DeviceTemplate>();
            FullDataModel.getInstance().commandsList = new ArrayList<CommandTemplate>();
            FullDataModel.getInstance().deviceGroupList = new ArrayList<DeviceGroupTemplate>();
//                FullDataModel.getInstance().sensorList = new ArrayList<SensorTemplate>();
            FullDataModel.getInstance().ruleList = new ArrayList<RuleTemplate>();
            FullDataModel.getInstance().userList = new ArrayList<UserTemplate>();
            FullDataModel.getInstance().userDeviceList = new ArrayList<UserDeviceTemplate>();

            NodeList responseElements = homeControlRootElement.getElementsByTagName("response");
            Node responseElement = responseElements.item(0);

            NodeList nodeElementsHouses = homeControlRootElement.getElementsByTagName("houses");
            for(int i = 0; i < nodeElementsHouses.getLength(); i++)
            {
                if(nodeElementsHouses.item(i).getNodeType() == Node.ELEMENT_NODE && (nodeElementsHouses.item(i).getParentNode().isSameNode(homeControlRootElement) | nodeElementsHouses.item(i).getParentNode().isSameNode(responseElement)))
                {
                    NodeList nodeElementsHousesInd = nodeElementsHouses.item(i).getChildNodes();
                    for(int j = 0; j < nodeElementsHousesInd.getLength(); j++)
                    {
                        Element houseElement = (Element) nodeElementsHousesInd.item(j);
                        HouseTemplate house = HouseTemplate.fromXmlStringStatic(Diverse.xmlToString(houseElement, true, false));
                        FullDataModel.getInstance().houseList.add(house);
                    }
                }
            }

        }
}
