package blanco.xml2meta;

import blanco.commons.util.BlancoXmlUtil;
import blanco.xml2meta.valueobject.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.w3c.dom.*;

import javax.xml.transform.dom.DOMResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 単一のXmlファイルを単一のMetaファイルに書き出す処理を定義します。
 */
public class BlancoXml2Meta {
    /**
     * BlancoXml2Metaが利用する変換定義情報
     */
    private BlancoXml2MetaDefStructure fDefStructure = null;

    public BlancoXml2Meta(BlancoXml2MetaDefStructure structure) {
        this.fDefStructure = structure;
    }

    /**
     * XMLファイルからMetaファイルを自動生成します。
     *
     * @param inputXmlFile
     *            XMLファイル
     * @param targetDirectory
     *            Metaファイル生成先ディレクトリ
     * @throws IOException
     *             入出力例外が発生した場合
     */
    public void process(final File inputXmlFile, final File targetDirectory)
            throws IOException {

        System.out.println("XMLファイル: " + inputXmlFile.getName() + " を処理します");

        // 入力XmlからDomを生成
        final DOMResult inputDom = BlancoXmlUtil.transformFile2Dom(inputXmlFile);

        // 入力XMLを順にパースしていく作業
        final Node rootNode = inputDom.getNode();
        if (rootNode instanceof Document) {
            // これが正常系。ドキュメントルートを取得
            final Document rootDocument = (Document) rootNode;

            // <workbook>があればworkbookを作る
            final Element nodeWorkbook = BlancoXmlUtil.getElement(rootDocument, "workbook");
            if(nodeWorkbook == null) {
                return;
            }
            System.out.println("Workbook found.");
            Workbook workbook = new SXSSFWorkbook();

            // 次に<sheet>がある
            final NodeList nodeListSheet = rootDocument.getElementsByTagName("sheet");
            final int sizeListSheet = nodeListSheet.getLength();
            for (int index = 0; index < sizeListSheet; index++) {
                // sheetを作る
                final Element nodeSheet = (Element) nodeListSheet.item(index);
                NamedNodeMap attrs = nodeSheet.getAttributes();
                String sheetName = attrs.getNamedItem("name").getNodeValue();
                Sheet sheet = workbook.createSheet(sheetName);
                // sheetの中身を書き込む
                System.out.println("sheet: " + sheetName + " を展開します");
                expandSheet(nodeSheet, sheet);
            }
            FileOutputStream out = new FileOutputStream(targetDirectory.getPath() + "/" + inputXmlFile.getName() + ".xlsx");
            workbook.write(out);
            workbook.close();
            out.close();
        }
    }

    /**
     * シートを展開します。
     * DOMElement:sheet の中には、何もないか、property-blockか、table-blockがあるはず
     *
     * @param nodeSheet
     *            sheetエレメント
     * @param sheet
     *            展開先のExcelシート
     */
    private void expandSheet(final Element nodeSheet, final Sheet sheet) {

        int rowCount = 2;

        // 定義書の情報defStructureから、propertyblockとtableblockを得る
        List<BlancoXml2MetaDefBlock> blocks = fDefStructure.getBlocks();

        if(blocks != null) {
            for (BlancoXml2MetaDefBlock block: blocks) {
                // 次にinputXmlの中から、各blockに紐付けられたelementの実体を探す
                final NodeList blockEntitys = nodeSheet.getElementsByTagName(block.getName());
                if (blockEntitys != null && blockEntitys.getLength() != 0) {
                    System.out.println("プロパティブロック" + block.getName() + "を見つけました。展開します。");
                    for (int index = 0; index < blockEntitys.getLength(); index++) {
                        final Element element = (Element) blockEntitys.item(index);
                        rowCount += expandBlock(block, element, sheet, rowCount);
                    }
                }
            }
        }
    }

    /**
     * ブロックがpropertyblockかtableblockかを判断して各ブロックに処理を投げます。
     *
     * @param blockDef
     *            blockの定義（propertyblockかtableblock）
     * @param nodeBlock
     *            blockの実体エレメント
     * @param sheet
     *            展開先のExcelシート
     * @param startRow
     *            シートに書き出すときの書き始めの行
     */
    private int expandBlock(BlancoXml2MetaDefBlock blockDef, Element nodeBlock, Sheet sheet, int startRow) {
        if(blockDef instanceof BlancoXml2MetaDefPropertyBlock) {
            BlancoXml2MetaDefPropertyBlock propertyBlock = (BlancoXml2MetaDefPropertyBlock) blockDef;
            return expandPropertyBlock(propertyBlock, nodeBlock, sheet, startRow);
        } else if(blockDef instanceof BlancoXml2MetaDefTableBlock) {
            BlancoXml2MetaDefTableBlock tableBlock = (BlancoXml2MetaDefTableBlock) blockDef;
            return expandTableBlock(tableBlock, nodeBlock, sheet, startRow);
        }
        return 0;
    }

    /**
     * propertyblockを処理します
     *
     * @param blockDef
     *            blockの定義（propertyblock）
     * @param nodeBlock
     *            blockの実体エレメント
     * @param sheet
     *            展開先のExcelシート
     * @param startRow
     *            シートに書き出すときの書き始めの行
     * @return この書き出しにより進んだExcelの行数
     *
     */
    private int expandPropertyBlock(BlancoXml2MetaDefPropertyBlock blockDef, Element nodeBlock, Sheet sheet, int startRow) {
        // プロパティブロックは定義書でpropertykeyに紐付けられた文字列をキーに持つ
        int rowCount = 1;

        // StartStringにあたるタイトルを書き込みます
        Row titleRow = sheet.createRow(startRow + rowCount++);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue(blockDef.getTitle().getName());

        List<BlancoXml2MetaDefBlockItem> rowStructures = blockDef.getItems();
        if(rowStructures != null) {
            for(BlancoXml2MetaDefBlockItem rowStructure: rowStructures) {
                Row row = sheet.createRow(startRow + rowCount++);
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(rowStructure.getName());
                Cell valueCell = row.createCell(rowStructure.getWaitX());
                valueCell.setCellValue(BlancoXmlUtil.getTextContent(nodeBlock, rowStructure.getId()));
            }
        }

        return rowCount;
    }

    /**
     * tableblockを処理します
     *
     * @param blockDef
     *            blockの定義（tableblock）
     * @param nodeBlock
     *            blockの実体エレメント
     * @param sheet
     *            展開先のExcelシート
     * @param startRow
     *            シートに書き出すときの書き始めの行
     * @return この書き出しにより進んだExcelの行数
     */
    private int expandTableBlock(BlancoXml2MetaDefTableBlock blockDef, Element nodeBlock, Sheet sheet, int startRow) {
        // プロパティブロックは定義書でpropertykeyに紐付けられた文字列をキーに持つ
        int rowCount = 1;

        // StartStringにあたるタイトルを書き込みます
        Row titleRow = sheet.createRow(startRow + rowCount++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(blockDef.getTitle().getName());

        // カラム名を書き出します
        Row columnNameRow = sheet.createRow(startRow + rowCount++);
        List<BlancoXml2MetaDefBlockItem> columnStructures = blockDef.getItems();
        if(columnStructures != null) {
            for(int index = 0; index < columnStructures.size(); index++) {
                BlancoXml2MetaDefBlockItem colunmStructure = columnStructures.get(index);
                Cell nameCell = columnNameRow.createCell(index);
                nameCell.setCellValue(colunmStructure.getName());
            }
        }

        // rownameで定義された一行分のデータを取り出して書き出す
        // @todo: nodeから1行分のデータを取り出すとき、インデックス順になっているわけではないので、インデックス順を保証する
        NodeList nodesOfRowData = nodeBlock.getElementsByTagName(blockDef.getRowname());
        if(nodesOfRowData != null) {
            for(int index = 0; index < nodesOfRowData.getLength(); index++) {
                Element aNodeOfRow = (Element) nodesOfRowData.item(index);
                Row row = sheet.createRow(startRow + rowCount++);
                if(columnStructures != null) {
                    for(int index2 = 0; index2 < columnStructures.size(); index2++) {
                        BlancoXml2MetaDefBlockItem colunmStructure = columnStructures.get(index2);
                        Cell cell = row.createCell(index2);
                        cell.setCellValue(BlancoXmlUtil.getTextContent(aNodeOfRow, colunmStructure.getId()));
                    }
                }
            }
        }
        return rowCount;
    }
}
