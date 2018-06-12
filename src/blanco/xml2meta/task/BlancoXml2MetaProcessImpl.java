/*
 * blanco Framework
 * Copyright (C) 2004-2009 IGA Tosiki
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.xml2meta.task;


import blanco.xml2meta.BlancoXml2Meta;
import blanco.xml2meta.BlancoXml2MetaConstants;
import blanco.xml2meta.BlancoXml2MetaXml2JavaClass;
import blanco.xml2meta.task.BlancoXml2MetaProcess;
import blanco.xml2meta.task.valueobject.BlancoXml2MetaProcessInput;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class BlancoXml2MetaProcessImpl implements BlancoXml2MetaProcess {
    /**
     * {@inheritDoc}
     */
    public int execute(final BlancoXml2MetaProcessInput input)
            throws IOException, IllegalArgumentException {
        System.out.println("- " + BlancoXml2MetaConstants.PRODUCT_NAME + " ("
                + BlancoXml2MetaConstants.VERSION + ")");


        try {
            final File fileXmldir = new File(input.getXmldir());
            if (fileXmldir.exists() == false) {
                throw new IllegalArgumentException("XML格納ディレクトリ["
                        + input.getXmldir() + "]が存在しません。");
            }

            final File[] xmlfiles = fileXmldir.listFiles();
            if (xmlfiles == null) {
                throw new IllegalArgumentException("ワークディレクトリ["
                        + fileXmldir.getAbsolutePath() + "]のファイル一覧の取得に失敗しました。");
            }

            final File fileDefXml = new File(input.getDefxml());
            if (fileDefXml.exists() == false) {
                throw new IllegalArgumentException("変換定義ファイル["
                        + input.getDefxml() + "]が存在しません。");
            }

            // 出力先ディレクトリを作成。
            File fileTargetDir = new File(input.getTargetdir());
            fileTargetDir.mkdir();

            // xml格納ディレクトリの中のxmlファイルを順に処理
            for (File xmlFile : xmlfiles) {
                if (xmlFile.getName().endsWith(".xml") == false) {
                    continue;
                }

                final BlancoXml2Meta xml2meta = new BlancoXml2Meta();
                xml2meta.setEncoding(input.getEncoding());
                xml2meta.process(xmlFile, fileTargetDir);
            }
            return 0;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean progress(final String argProgressMessage) {
        System.out.println(argProgressMessage);
        return false;
    }
}
