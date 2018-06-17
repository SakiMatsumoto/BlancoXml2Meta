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

        if(nodesOfProrertyBlock != null) { // propertyblockを見つけた
            // ひとつずつpropertyblockを処理します
            for (int index = 0; index < nodesOfProrertyBlock.getLength(); index++) {
                Element aNodeOfBlock = (Element) nodesOfProrertyBlock.item(index);
                BlancoXml2MetaDefPropertyBlock block = parsePropertyBlock(aNodeOfBlock);
                blocks.add(block);
            }
        }

        // テーブルブロックのリストを得ます
        NodeList nodesOfTableBlock = ((Document) rootNode).getElementsByTagName("tableblock");

        if(nodesOfTableBlock != null) { // tableblockを見つけた
            //ひとつずつブロックを処理します
            for (int index = 0; index < nodesOfTableBlock.getLength(); index++) {
                Element aNodeOfBlock = (Element) nodesOfTableBlock.item(index);
                BlancoXml2MetaDefTableBlock block = parseTableBlock(aNodeOfBlock);
                blocks.add(block);
            }
        }

        BlancoXml2MetaDefStructure structure = new BlancoXml2MetaDefStructure();
        structure.setBlocks(blocks);

        return structure;
    }

    private BlancoXml2MetaDefPropertyBlock parsePropertyBlock(Element aNodeOfBlock) {
        BlancoXml2MetaDefPropertyBlock block = new BlancoXml2MetaDefPropertyBlock();
        NamedNodeMap attrs = aNodeOfBlock.getAttributes();

        // ブロックのID（必須要素）
        block.setName(attrs.getNamedItem("name").getNodeValue());

        // タイトル
        BlancoXml2MetaDefBlockTitle title = new BlancoXml2MetaDefBlockTitle();
        title.setName(BlancoXmlUtil.getTextContent(aNodeOfBlock, "startstring"));

        Node nodeOfWaitX = attrs.getNamedItem("waitX");
        if(nodeOfWaitX != null) {
            title.setWaitX(Integer.parseInt(nodeOfWaitX.getNodeValue()));
        }

        Node nodeOfWaitY = attrs.getNamedItem("waitY");
        if(nodeOfWaitY != null) {
            title.setWaitY(Integer.parseInt(nodeOfWaitY.getNodeValue()));
        }
        block.setTitle(title);

        // ブロックの中の<propertykey>を解析します
        ArrayList<BlancoXml2MetaDefBlockItem> items = new ArrayList<BlancoXml2MetaDefBlockItem>();
        NodeList nodesOfItem = aNodeOfBlock.getElementsByTagName("propertykey");
        for (int index = 0; index < nodesOfItem.getLength(); index++) {
            Element aNodeOfItem = (Element) nodesOfItem.item(index);
            BlancoXml2MetaDefBlockItem item = parseBlockItem(aNodeOfItem);
            items.add(item);
        }
        block.setItems(items);
        return block;
    }

    private BlancoXml2MetaDefTableBlock parseTableBlock(Element aNodeOfBlock) {
        BlancoXml2MetaDefTableBlock block = new BlancoXml2MetaDefTableBlock();
        NamedNodeMap attrs = aNodeOfBlock.getAttributes();

        // ブロックのID（必須要素）
        block.setName(attrs.getNamedItem("name").getNodeValue());

        // テーブルの独自要素rowname
        block.setRowname(attrs.getNamedItem("rowname").getNodeValue());

        // タイトル
        BlancoXml2MetaDefBlockTitle title = new BlancoXml2MetaDefBlockTitle();
        title.setName(BlancoXmlUtil.getTextContent(aNodeOfBlock, "startstring"));

        Node nodeOfWaitX = attrs.getNamedItem("waitX");
        if(nodeOfWaitX != null) {
            title.setWaitX(Integer.parseInt(nodeOfWaitX.getNodeValue()));
        }

        Node nodeOfWaitY = attrs.getNamedItem("waitY");
        if(nodeOfWaitY != null) {
            title.setWaitY(Integer.parseInt(nodeOfWaitY.getNodeValue()));
        }
        block.setTitle(title);

        // ブロックの中の<tablecolumn>を解析します
        ArrayList<BlancoXml2MetaDefBlockItem> items = new ArrayList<BlancoXml2MetaDefBlockItem>();
        NodeList nodesOfItem = aNodeOfBlock.getElementsByTagName("tablecolumn");
        for (int index = 0; index < nodesOfItem.getLength(); index++) {
            Element aNodeOfItem = (Element) nodesOfItem.item(index);
            BlancoXml2MetaDefBlockItem item = parseBlockItem(aNodeOfItem);
            items.add(item);
        }
        block.setColumnCount(items.size());
        block.setItems(items);
        return block;
    }

    private BlancoXml2MetaDefBlockItem parseBlockItem(Element aNodeOfItem) {
        BlancoXml2MetaDefBlockItem item = new BlancoXml2MetaDefBlockItem();
        NamedNodeMap attrs2 = aNodeOfItem.getAttributes();
        item.setId(attrs2.getNamedItem("name").getNodeValue());
        Node nodeWaitX = attrs2.getNamedItem("waitX");
        if (nodeWaitX != null) {
            item.setWaitX(Integer.parseInt(nodeWaitX.getNodeValue()));
        }
        Node nodeWaitY = attrs2.getNamedItem("waitY");
        if (nodeWaitY != null) {
            item.setWaitY(Integer.parseInt(nodeWaitY.getNodeValue()));
        }
        item.setName(BlancoXmlUtil.getTextContent(aNodeOfItem, "value"));
        return item;
    }

}
