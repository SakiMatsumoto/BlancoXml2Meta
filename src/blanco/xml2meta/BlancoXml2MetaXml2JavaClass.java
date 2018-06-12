/*
 * blanco Framework
 * Copyright (C) 2004-2009 IGA Tosiki
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.xml2meta;

import blanco.cg.BlancoCgObjectFactory;
import blanco.cg.transformer.BlancoCgTransformerFactory;
import blanco.cg.valueobject.BlancoCgClass;
import blanco.cg.valueobject.BlancoCgField;
import blanco.cg.valueobject.BlancoCgMethod;
import blanco.cg.valueobject.BlancoCgSourceFile;
import blanco.commons.util.BlancoJavaSourceUtil;
import blanco.commons.util.BlancoStringUtil;
import blanco.commons.util.BlancoXmlUtil;
import blanco.meta2xml.valueobject.BlancoMeta2XmlStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * blancoValueObjectの主たるクラス。
 * 
 * blancoValueObjectを表現するXMLファイルから Javaソースコードを自動生成します。
 * 
 * @author IGA Tosiki
 */
public class BlancoXml2MetaXml2JavaClass {
    /**
     * 内部的に利用するblancoCg用ファクトリ。
     */
    private BlancoCgObjectFactory fCgFactory = null;

    /**
     * 内部的に利用するblancoCg用ソースファイル情報。
     */
    private BlancoCgSourceFile fCgSourceFile = null;

    /**
     * 内部的に利用するblancoCg用クラス情報。
     */
    private BlancoCgClass fCgClass = null;

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

        expandJavaSource(processStructure, directoryTarget);
    }

    /**
     * 収集された情報を元に、Javaソースコードを出力します。
     *
     * @param processStructure
     *            処理構造
     * @param directoryTarget
     *            出力先フォルダ。
     */
    private void expandJavaSource(
            final BlancoMeta2XmlStructure processStructure,
            final File directoryTarget) {
        // 従来と互換性を持たせるため、/mainサブフォルダに出力します。
        final File fileBlancoMain = new File(directoryTarget.getAbsolutePath()
                + "/main");

        fCgFactory = BlancoCgObjectFactory.getInstance();
        fCgSourceFile = fCgFactory.createSourceFile(processStructure
                .getPackage(), "このソースコードは blanco Frameworkによって自動生成されています。");
        fCgSourceFile.setEncoding(fEncoding);
        if (processStructure.getFileDescription() != null) {
            fCgSourceFile.getLangDoc().getDescriptionList().add(
                    processStructure.getFileDescription());
        }

        fCgClass = fCgFactory.createClass(processStructure.getName(),
                BlancoStringUtil.null2Blank(processStructure.getDescription()));
        fCgSourceFile.getClassList().add(fCgClass);

        fCgSourceFile.getImportList().add("java.io.BufferedInputStream");
        fCgSourceFile.getImportList().add("java.io.BufferedOutputStream");
        fCgSourceFile.getImportList().add("java.io.FileInputStream");
        fCgSourceFile.getImportList().add("java.io.FileOutputStream");
        fCgSourceFile.getImportList().add("java.io.IOException");
        fCgSourceFile.getImportList().add("java.io.InputStream");
        fCgSourceFile.getImportList().add("java.io.OutputStream");
        fCgSourceFile.getImportList().add(
                "javax.xml.transform.TransformerException");
        fCgSourceFile.getImportList().add(
                "blanco.commons.calc.parser.BlancoCalcParser");

        {
            final BlancoCgField field = fCgFactory.createField(
                    "fCacheMeta2Xml", "boolean",
                    "定義書メタファイルから中間XMLファイルへの変換をキャッシュで済ますかどうかのフラグ。");
            fCgClass.getFieldList().add(field);
            field.setAccess("protected");
            field.setDefault("false");
        }

        {
            final BlancoCgField field = fCgFactory.createField(
                    "fCacheMeta2XmlCount", "int",
                    "定義書メタファイルから中間XMLファイルへの変換をキャッシュで済ませた回数。");
            fCgClass.getFieldList().add(field);
            field.setAccess("protected");
            field.setDefault("0");
        }

        {
            final BlancoCgField field = fCgFactory.createField(
                    "fCacheMetaDefXml", "byte[]",
                    "クラスローダからの定義書構造XMLファイルの読込回数を減らすためのキャッシュ。");
            fCgClass.getFieldList().add(field);
            field.setAccess("protected");
            field.setDefault("null");
        }

        {
            final BlancoCgMethod method = fCgFactory.createMethod(
                    "setCacheMeta2Xml",
                    "定義書メタファイルから中間XMLファイルへの変換をキャッシュで済ますかどうかのフラグを指定します。");
            fCgClass.getMethodList().add(method);

            method.getParameterList().add(
                    fCgFactory.createParameter("argCacheMeta2Xml", "boolean",
                            "定義書メタファイルから中間XMLファイルへの変換をキャッシュで済ますかどうか。"));

            final List<String> listLine = method.getLineList();
            listLine.add("fCacheMeta2Xml = argCacheMeta2Xml;");
        }

        {
            final BlancoCgMethod methodProcess1 = fCgFactory.createMethod(
                    "process", "ExcelファイルのストリームをXMLファイルのストリームに変換します。");
            fCgClass.getMethodList().add(methodProcess1);
            methodProcess1.getLangDoc().getDescriptionList().add(
                    "定義ファイルは内部的にパスを保持しています。");
            methodProcess1.getParameterList().add(
                    fCgFactory.createParameter("inStreamMetaSource",
                            "java.io.InputStream", "メタファイルの入力ストリーム。"));
            methodProcess1.getParameterList().add(
                    fCgFactory.createParameter("outStreamTarget",
                            "java.io.OutputStream", "XMLファイルの出力ストリーム。"));
            methodProcess1.getThrowList().add(
                    fCgFactory.createException("java.io.IOException",
                            "入出力例外が発生した場合。"));
            methodProcess1.getThrowList().add(
                    fCgFactory.createException(
                            "javax.xml.transform.TransformerException",
                            "XML変換例外が発生した場合。"));
            final List<String> listLine = methodProcess1
                    .getLineList();
            listLine.add("if (inStreamMetaSource == null) {");

            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: inStreamMetaSource is null.\");");
            listLine.add("}");
            listLine.add("if (outStreamTarget == null) {");
            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: outStreamTarget is null.\");");
            listLine.add("}");
            listLine.add("");
            listLine.add("if (fCacheMetaDefXml == null) {");
            listLine.add("// このクラス自身とおなじクラスローダからXML設定ファイルのロードをおこないます。");
            listLine
                    .add("final InputStream meta2xmlStream = getClass().getClassLoader().getResourceAsStream(\""
                            + processStructure.getConvertDefFile() + "\");");
            listLine.add("if (meta2xmlStream == null) {");
            listLine
                    .add("throw new IllegalArgumentException(\""
                            + processStructure.getName() + ": リソース["
                            + processStructure.getConvertDefFile()
                            + "]の取得に失敗しました.\");");
            listLine.add("}");
            fCgSourceFile.getImportList().add("java.io.ByteArrayOutputStream");
            fCgSourceFile.getImportList().add("java.io.ByteArrayInputStream");
            listLine
                    .add("final ByteArrayOutputStream outStream = new ByteArrayOutputStream();");
            listLine.add("final byte[] bufWrk = new byte[8192];");
            listLine.add("for (;;) {");
            listLine.add("final int readLength = meta2xmlStream.read(bufWrk);");
            listLine.add("if (readLength <= 0) {");
            listLine.add("break;");
            listLine.add("}");
            listLine.add("outStream.write(bufWrk, 0, readLength);");
            listLine.add("}");
            listLine.add("outStream.flush();");
            listLine.add("meta2xmlStream.close();");
            listLine.add("fCacheMetaDefXml = outStream.toByteArray();");

            listLine.add("}");
            listLine.add("");
            listLine
                    .add("InputStream inStreamDef = new ByteArrayInputStream(fCacheMetaDefXml);");
            listLine.add("try {");
            listLine
                    .add("new BlancoCalcParser().process(inStreamDef, inStreamMetaSource, outStreamTarget);");
            listLine.add("} finally {");
            listLine.add("if (inStreamDef != null) {");
            listLine.add("inStreamDef.close();");
            listLine.add("}");
            listLine.add("}");
        }

        {
            final BlancoCgMethod methodProcess2 = fCgFactory.createMethod(
                    "process", "ExcelファイルをXMLファイルに変換します。");
            fCgClass.getMethodList().add(methodProcess2);
            methodProcess2.getParameterList().add(
                    fCgFactory.createParameter("fileMeta", "java.io.File",
                            "メタファイルの入力ファイル。"));
            methodProcess2.getParameterList().add(
                    fCgFactory.createParameter("fileOutput", "java.io.File",
                            "XMLファイルの出力。"));
            methodProcess2.getThrowList().add(
                    fCgFactory.createException("java.io.IOException",
                            "入出力例外が発生した場合。"));
            methodProcess2.getThrowList().add(
                    fCgFactory.createException(
                            "javax.xml.transform.TransformerException",
                            "XML変換例外が発生した場合。"));
            final List<String> listLine = methodProcess2
                    .getLineList();

            listLine.add("if (fileMeta == null) {");
            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: fileMeta is null.\");");
            listLine.add("}");
            listLine.add("if (fileOutput == null) {");
            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: fileOutput is null.\");");
            listLine.add("}");
            listLine.add("if (fileMeta.exists() == false) {");
            listLine
                    .add("throw new IllegalArgumentException(\""
                            + processStructure.getName()
                            + ": Invalid argument: file file [\" + fileMeta.getAbsolutePath() + \"] not found.\");");
            listLine.add("}");
            listLine.add("");
            listLine
                    .add("if (fCacheMeta2Xml && fileMeta.lastModified() < fileOutput.lastModified()) {");
            listLine.add("// キャッシュを利用して、処理をスキップします。");
            listLine.add("fCacheMeta2XmlCount++;");
            listLine.add("return;");
            listLine.add("}");
            listLine.add("");
            listLine.add("InputStream inStream = null;");
            listLine.add("OutputStream outStream = null;");
            listLine.add("try {");
            listLine
                    .add("inStream = new BufferedInputStream(new FileInputStream(fileMeta), 8192);");
            listLine
                    .add("outStream = new BufferedOutputStream(new FileOutputStream(fileOutput), 8192);");
            listLine.add("");
            listLine.add("// ストリームの準備ができたので、実際の処理をおこないます。");
            listLine.add("process(inStream, outStream);");
            listLine.add("");
            listLine.add("outStream.flush();");
            listLine.add("} finally {");
            listLine.add("if (inStream != null) {");
            listLine.add("inStream.close();");
            listLine.add("}");
            listLine.add("if (outStream != null) {");
            listLine.add("outStream.close();");
            listLine.add("}");
            listLine.add("}");
        }

        {
            final BlancoCgMethod methodProcessDirectory = fCgFactory
                    .createMethod("processDirectory",
                            "指定ディレクトリ内のExcelファイルをXMLファイルに変換します。");
            fCgClass.getMethodList().add(methodProcessDirectory);
            methodProcessDirectory.getLangDoc().getDescriptionList().add(
                    "指定されたフォルダ内の拡張子[" + processStructure.getInputFileExt()
                            + "]のファイルを処理します。<br>");
            if(processStructure.getInputFileExtSub() != null) {
                methodProcessDirectory.getLangDoc().getDescriptionList().add(
                        "指定されたフォルダ内の拡張子[" + processStructure.getInputFileExtSub()
                                + "]のファイルを処理します。<br>");
            }
            methodProcessDirectory.getLangDoc().getDescriptionList().add(
                    "処理したデータは もとのファイル名に拡張子["
                            + processStructure.getOutputFileExt()
                            + "]を付与したファイルへ保存します。");
            methodProcessDirectory.getParameterList().add(
                    fCgFactory.createParameter("fileMetadir", "java.io.File",
                            "メタファイルが格納されている入力ディレクトリ。"));
            methodProcessDirectory.getParameterList().add(
                    fCgFactory.createParameter("targetDirectory",
                            "java.lang.String", "出力ディレクトリ。"));
            methodProcessDirectory.getThrowList().add(
                    fCgFactory.createException("java.io.IOException",
                            "入出力例外が発生した場合。"));
            methodProcessDirectory.getThrowList().add(
                    fCgFactory.createException(
                            "javax.xml.transform.TransformerException",
                            "XML変換例外が発生した場合。"));
            final List<String> listLine = methodProcessDirectory
                    .getLineList();

            listLine.add("System.out.println(\"m2x: begin.\");");
            listLine.add("final long startMills = System.currentTimeMillis();");
            listLine.add("long totalFileCount = 0;");
            listLine.add("long totalFileBytes = 0;");
            listLine.add("");
            listLine.add("if (fileMetadir == null) {");
            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: fileMetadir is null.\");");
            listLine.add("}");
            listLine.add("if (targetDirectory == null) {");
            listLine.add("throw new IllegalArgumentException(\""
                    + processStructure.getName()
                    + ": Invalid argument: targetDirectory is null.\");");
            listLine.add("}");
            listLine.add("if (fileMetadir.exists() == false) {");
            listLine
                    .add("throw new IllegalArgumentException(\""
                            + processStructure.getName()
                            + ": Invalid argument: file [\" + fileMetadir.getAbsolutePath() + \"] not found.\");");
            listLine.add("}");
            listLine
                    .add("final File fileTargetDirectory = new File(targetDirectory);");
            listLine.add("if (fileTargetDirectory.exists() == false) {");
            listLine.add("// 出力先ディレクトリが存在しないので、事前に作成します。");
            listLine.add("fileTargetDirectory.mkdirs();");
            listLine.add("}");
            listLine.add("");
            listLine.add("// 指定されたディレクトリのファイル一覧を取得します。");
            listLine.add("final File[] fileMeta = fileMetadir.listFiles();");
            listLine.add("if (fileMeta == null) {");
            listLine
                    .add("throw new IllegalArgumentException(\"BlancoMeta2XmlProcessMeta2Xml: list directory [\" + fileMetadir.getAbsolutePath() + \"] is failed.\");");
            listLine.add("}");
            listLine
                    .add("for (int index = 0; index < fileMeta.length; index++) {");
            listLine.add("if ((fileMeta[index].getName().endsWith(\""
                    + BlancoJavaSourceUtil
                    .escapeStringAsJavaSource(processStructure
                            .getInputFileExt()) + "\") == false");
            if(processStructure.getInputFileExtSub() != null){
                listLine.add(" && fileMeta[index].getName().endsWith(\""
                        + BlancoJavaSourceUtil
                        .escapeStringAsJavaSource(processStructure
                                .getInputFileExtSub()) + "\") == false)");
            }
            /* added by KINOKO 読み込みを除外すべきファイル（一時ファイル）をスキップする
             * @TODO startsWithをmatchesにするべきである
            */
            listLine.add("|| fileMeta[index].getName().startsWith(\""
                    + BlancoJavaSourceUtil
                    .escapeStringAsJavaSource(processStructure
                            .getExcludedFileRegex()) + "\")");
            listLine.add(") {");
            listLine.add("// ファイルの拡張子が処理すべきものとは異なるため処理をスキップします。。");
            listLine.add("continue;");
            listLine.add("}");
            listLine.add("");

            listLine
                    .add("if (progress(index + 1, fileMeta.length, fileMeta[index].getName()) == false) {");
            listLine.add("// 進捗表示から処理中断の指示が来たので、処理中断します。");
            listLine.add("break;");
            listLine.add("}");

            listLine.add("");
            listLine.add("try {");
            listLine.add("totalFileCount++;");
            listLine.add("totalFileBytes += fileMeta[index].length();");
            listLine
                    .add("process(fileMeta[index], new File(targetDirectory + \"/\" + fileMeta[index].getName() + \""
                            + BlancoJavaSourceUtil
                                    .escapeStringAsJavaSource(processStructure
                                            .getOutputFileExt()) + "\"));");
            listLine.add("} catch (Exception ex) {");
            listLine.add("ex.printStackTrace();");
            listLine
                    .add("throw new IllegalArgumentException(\""
                            + processStructure.getName()
                            + ": Exception occurs during processing the file [\" + fileMeta[index].getAbsolutePath() + \"]. \" + ex.toString());");
            listLine.add("}");
            listLine.add("}");
            listLine.add("");
            listLine.add("if (fCacheMeta2Xml) {");
            listLine
                    .add("System.out.println(\"m2x: cache: \" + fCacheMeta2XmlCount + \" file skipped.\");");
            listLine.add("}");
            listLine
                    .add("final long costMills = System.currentTimeMillis() - startMills + 1;");
            listLine
                    .add("System.out.println(\"m2x: end: \" + (costMills / 1000) + \" sec, \" + totalFileCount + \" file, \" + totalFileBytes + \" byte (\" + (totalFileBytes * 1000 / costMills) + \" byte/sec).\");");
        }

        {
            final BlancoCgMethod methodProgress = fCgFactory.createMethod(
                    "progress", "処理の進捗を示します。");
            fCgClass.getMethodList().add(methodProgress);
            methodProgress.setAccess("protected");
            methodProgress.getLangDoc().getDescriptionList().add(
                    "進捗表示をさせたい場合には継承して処理を作りこみます。");
            methodProgress.getParameterList().add(
                    fCgFactory.createParameter("progressCurrent", "int",
                            "現在処理している件数の番号。"));
            methodProgress.getParameterList().add(
                    fCgFactory
                            .createParameter("progressTotal", "int", "総処理件数。"));
            methodProgress.getParameterList().add(
                    fCgFactory.createParameter("progressItem",
                            "java.lang.String", "処理しているアイテム名。"));
            methodProgress.setReturn(fCgFactory.createReturn("boolean",
                    "処理を続行してよいかどうか。falseなら処理中断。"));
            final List<String> listLine = methodProgress
                    .getLineList();

            listLine.add("// 常に処理続行を示す true を戻します。");
            listLine.add("return true;");
        }

        BlancoCgTransformerFactory.getJavaSourceTransformer().transform(
                fCgSourceFile, fileBlancoMain);
    }
}
