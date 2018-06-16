package blanco.xml2meta;

import blanco.commons.calc.parser.block.BlancoCalcParserPropertyBlock;
import blanco.commons.util.BlancoXmlUtil;
import blanco.xml2meta.valueobject.*;
import org.w3c.dom.*;

import javax.xml.transform.dom.DOMResult;
import java.io.File;
import java.util.ArrayList;

public class BlancoXml2MetaDefParser {

    public BlancoXml2MetaDefStructure parse(File defFile) {
        // 定義ファイルをDomにする
        DOMResult defDom = BlancoXmlUtil.transformFile2Dom(defFile);
        final Node rootNode = defDom.getNode();
        if(rootNode instanceof Document) {
            Document document = (Document) rootNode;
        } else {
            return null;
        }

        ArrayList<BlancoXml2MetaDefBlock> blocks = new ArrayList<BlancoXml2MetaDefBlock>();

        // プロパティブロックのリストを得ます
        NodeList nodesOfProrertyBlock = ((Document) rootNode).getElementsByTagName("propertyblock");
        if(nodesOfProrertyBlock != null) {
            for (int index = 0; index < nodesOfProrertyBlock.getLength(); index++) {
                BlancoXml2MetaDefPropertyBlock block = new BlancoXml2MetaDefPropertyBlock();
                Element aNodeOfBlock = (Element) nodesOfProrertyBlock.item(index);
                NamedNodeMap attrs = aNodeOfBlock.getAttributes();
                block.setName(attrs.getNamedItem("name").getNodeValue());
                BlancoXml2MetaDefBlockTitle title = new BlancoXml2MetaDefBlockTitle();
                title.setName(BlancoXmlUtil.getTextContent(aNodeOfBlock, "startstring"));
//                title.setWaitX(Integer.parseInt(attrs.getNamedItem("waitX").getNodeValue()));
//                title.setWaitY(Integer.parseInt(attrs.getNamedItem("waitY").getNodeValue()));
                block.setTitle(title);
                ArrayList<BlancoXml2MetaDefBlockItem> items = new ArrayList<BlancoXml2MetaDefBlockItem>();
                NodeList nodesOfItem = aNodeOfBlock.getElementsByTagName("propertykey");
                for (int index2 = 0; index2 < nodesOfItem.getLength(); index2++) {
                    Element aNodeOfItem = (Element) nodesOfItem.item(index2);
                    NamedNodeMap attrs2 = aNodeOfItem.getAttributes();
                    BlancoXml2MetaDefBlockItem item = new BlancoXml2MetaDefBlockItem();
                    item.setId(attrs2.getNamedItem("name").getNodeValue());
//                    item.setWaitX(Integer.parseInt(attrs2.getNamedItem("waitX").getNodeValue()));
//                    item.setWaitY(Integer.parseInt(attrs2.getNamedItem("waitY").getNodeValue()));
                    item.setName(BlancoXmlUtil.getTextContent(aNodeOfItem, "value"));
                    items.add(item);
                }
                block.setItems(items);
                blocks.add(block);
            }
        }

        // テーブルブロックのリストを得ます
        NodeList nodesOfTableBlock = ((Document) rootNode).getElementsByTagName("tableblock");
        if(nodesOfTableBlock != null) {
            for (int index = 0; index < nodesOfTableBlock.getLength(); index++) {

                //ひとつずつブロックを処理します
                BlancoXml2MetaDefTableBlock block = new BlancoXml2MetaDefTableBlock();
                Element aNodeOfBlock = (Element) nodesOfTableBlock.item(index);
                NamedNodeMap attrs = aNodeOfBlock.getAttributes();
                block.setRowname(attrs.getNamedItem("rowname").getNodeValue());
                block.setName(attrs.getNamedItem("name").getNodeValue());
                BlancoXml2MetaDefBlockTitle title = new BlancoXml2MetaDefBlockTitle();
                title.setName(BlancoXmlUtil.getTextContent(aNodeOfBlock, "startstring"));
//                title.setWaitX(Integer.parseInt(attrs.getNamedItem("waitX").getNodeValue()));
//                title.setWaitY(Integer.parseInt(attrs.getNamedItem("waitY").getNodeValue()));
                block.setTitle(title);

                // ブロックの中の<tablecolumn>を解析します
                ArrayList<BlancoXml2MetaDefBlockItem> items = new ArrayList<BlancoXml2MetaDefBlockItem>();
                NodeList nodesOfItem = aNodeOfBlock.getElementsByTagName("tablecolumn");
                for (int index2 = 0; index2 < nodesOfItem.getLength(); index2++) {
                    Element aNodeOfItem = (Element) nodesOfItem.item(index2);
                    NamedNodeMap attrs2 = aNodeOfItem.getAttributes();
                    BlancoXml2MetaDefBlockItem item = new BlancoXml2MetaDefBlockItem();
                    item.setId(attrs2.getNamedItem("name").getNodeValue());
//                    item.setWaitX(Integer.parseInt(attrs2.getNamedItem("waitX").getNodeValue()));
//                    item.setWaitY(Integer.parseInt(attrs2.getNamedItem("waitY").getNodeValue()));
                    item.setName(BlancoXmlUtil.getTextContent(aNodeOfItem, "value"));
                    items.add(item);
                }
                block.setItems(items);
                blocks.add(block);
            }
        }

        BlancoXml2MetaDefStructure structure = new BlancoXml2MetaDefStructure();
        structure.setBlocks(blocks);
        System.out.println("ブロック数: " + blocks.size());

        return structure;
    }
}
