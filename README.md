# BlancoXml2Metaの使用方法

## 準備
1. 最新のblancoxml2meta-x.x.x.jarを入手します。  
手元にない場合は、BlancoXml2Metaプロジェクトをダウンロードして、コマンドプロンプトで
```ant prepare```
を実行すると、プロジェクトディレクトリのルートにblancoxml2meta-x.x.x.jarが作成されます。

1. 入手したjarファイルをlib.antに追加します。
1. build.xmlに以下を追加します。

```
<taskdef name="xml2meta" classname="blanco.xml2meta.task.BlancoXml2MetaTask">
<classpath>
    <fileset dir="lib" includes="*.jar" />
    <fileset dir="lib.ant" includes="*.jar" />
</classpath>
</taskdef>

<target name="xml2meta">
    <xml2meta xmldir="tmp/valueobject" targetdir="meta/out" defxml="/Users/superkinoko/work/ueo/blancoXml2Meta/defXmlFiles/BlancoValueObjectMeta2Xml.xml" encoding="${encoding}" verbose="true" />
</target>
```

## 入力パラメータ

1. xmldir:(入力必須)   
 Excelファイルに変換したいXMLファイルがあるディレクトリ。
 
1. defxml:(入力必須)   
 ExcelファイルをXMLファイルに変換したときに使った変換定義ファイル。  
 フルパスを指定してください。
  
1. defxml:(デフォルト meta/out)  
生成されたExcelファイルの出力先フォルダ。

## 変換処理
コマンドプロンプトで```ant xml2meta```と入力すると変換されます。