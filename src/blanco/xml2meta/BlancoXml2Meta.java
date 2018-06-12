package blanco.xml2meta;

import blanco.commons.util.BlancoXmlUtil;
import blanco.meta2xml.valueobject.BlancoMeta2XmlStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMResult;
import java.io.File;
import java.io.IOException;

public class BlancoXml2Meta {

    /**
     * 自動生成するソースファイルの文字エンコーディング。
     */
    private String fEncoding = null;

    public void setEncoding(final String argEncoding) {
        fEncoding = argEncoding;
    }

    /**
     * ValueObjectを表現するXMLファイルから Javaソースコードを自動生成します。
     *
     * @param metaXmlSourceFile
     *            ValueObjectに関するメタ情報が含まれているXMLファイル
     * @param directoryTarget
     *            ソースコード生成先ディレクトリ
     * @throws IOException
     *             入出力例外が発生した場合
     */
    public void process(final File metaXmlSourceFile, final File directoryTarget)
            throws IOException {

        final DOMResult result = BlancoXmlUtil
                .transformFile2Dom(metaXmlSourceFile);

        final Node rootNode = result.getNode();
        if (rootNode instanceof Document) {
            // これが正常系。ドキュメントルートを取得
            final Document rootDocument = (Document) rootNode;
            final NodeList listSheet = rootDocument
                    .getElementsByTagName("sheet");
            final int sizeListSheet = listSheet.getLength();
            for (int index = 0; index < sizeListSheet; index++) {
                final Element elementCommon = BlancoXmlUtil.getElement(
                        listSheet.item(index), "blancometa2xml-process-common");
                if (elementCommon == null) {
                    // commonが無い場合にはスキップします。
                    continue;
                }

                final String name = BlancoXmlUtil.getTextContent(elementCommon,
                        "name");
                if (name == null || name.trim().length() == 0) {
                    continue;
                }

                expandSheet(elementCommon, directoryTarget);
            }
        }
    }

    /**
     * シートを展開します。
     *
     * @param elementCommon
     *            現在処理しているCommonノード
     * @param directoryTarget
     *            出力先フォルダ。
     */
    private void expandSheet(final Element elementCommon,
                             final File directoryTarget) {
        final BlancoMeta2XmlStructure processStructure = new BlancoMeta2XmlStructure();
        processStructure.setName(BlancoXmlUtil.getTextContent(elementCommon,
                "name"));
        processStructure.setPackage(BlancoXmlUtil.getTextContent(elementCommon,
                "package"));
        if (processStructure.getPackage() == null
                || processStructure.getPackage().trim().length() == 0) {
            throw new IllegalArgumentException("メタファイル-XML変換処理定義 クラス名["
                    + processStructure.getName() + "]のパッケージが指定されていません。");
        }

        if (BlancoXmlUtil.getTextContent(elementCommon, "description") != null) {
            processStructure.setDescription(BlancoXmlUtil.getTextContent(
                    elementCommon, "description"));
        }
        if (BlancoXmlUtil.getTextContent(elementCommon, "fileDescription") != null) {
            processStructure.setFileDescription(BlancoXmlUtil.getTextContent(
                    elementCommon, "fileDescription"));
        }

        processStructure.setConvertDefFile(BlancoXmlUtil.getTextContent(
                elementCommon, "convertDefFile"));
        if (processStructure.getConvertDefFile() == null
                || processStructure.getConvertDefFile().trim().length() == 0) {
            throw new IllegalArgumentException("メタファイル-XML変換処理定義 クラス名["
                    + processStructure.getName() + "]の変換定義ファイルが指定されていません。");
        }

        if (BlancoXmlUtil.getTextContent(elementCommon, "inputFileExt") != null) {
            processStructure.setInputFileExt(BlancoXmlUtil.getTextContent(
                    elementCommon, "inputFileExt"));
        }
        if (BlancoXmlUtil.getTextContent(elementCommon, "outputFileExt") != null) {
            processStructure.setOutputFileExt(BlancoXmlUtil.getTextContent(
                    elementCommon, "outputFileExt"));
        }
        if (BlancoXmlUtil.getTextContent(elementCommon, "inputFileExtSub") != null) {
            processStructure.setInputFileExtSub(BlancoXmlUtil.getTextContent(
                    elementCommon, "inputFileExtSub"));
        }
        /* added by KINOKO */
        if (BlancoXmlUtil.getTextContent(elementCommon, "excludedFileRegex") != null) {
            processStructure.setExcludedFileRegex(BlancoXmlUtil.getTextContent(
                    elementCommon, "excludedFileRegex"));
        }

        expandMeta(processStructure, directoryTarget);
    }

    private void expandMeta(
            final BlancoMeta2XmlStructure processStructure,
            final File directoryTarget) {
        System.out.println("xmlをMetaに展開します");

    }

}
