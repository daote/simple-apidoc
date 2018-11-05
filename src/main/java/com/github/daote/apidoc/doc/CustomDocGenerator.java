package com.github.daote.apidoc.doc;

import cn.hutool.core.io.FileUtil;
import com.github.daote.apidoc.DocContext;
import com.github.daote.apidoc.LogUtils;
import com.github.daote.apidoc.Utils;
import com.github.daote.apidoc.parser.AbsControllerParser;
import com.github.daote.apidoc.parser.ControllerNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomDocGenerator {
    private AbsControllerParser controllerParser;
    private CustomDocBuilder controllerDocBuilder;
    private List<String> docFileNameList = new ArrayList<>();
    private List<ControllerNode> controllerNodeList = new ArrayList<>();

    public CustomDocGenerator() {
        this.controllerParser = DocContext.controllerParser();
        this.controllerDocBuilder = new CustomDocBuilder();
    }

    /**
     * generate api Docs
     */
    public void generateDocs(){
        LogUtils.info("generate api docs start...");
        generateControllersDocs();
        generateIndex(docFileNameList);
        LogUtils.info("generate api docs done !!!");
    }

    private void generateControllersDocs(){
        File[] controllerFiles = DocContext.getControllerFiles();
        for (File controllerFile : controllerFiles) {

            LogUtils.info("start to parse controller file : %s", controllerFile.getName());
            ControllerNode controllerNode = controllerParser.parse(controllerFile);
            if(controllerNode.getRequestNodes().isEmpty()){
                continue;
            }
            controllerNodeList.add(controllerNode);
            LogUtils.info("start to generate docs for controller file : %s", controllerFile.getName());

            String docName = controllerNode.getDescription();
            docFileNameList.add(docName);

            LogUtils.info("success to generate docs for controller file : %s", controllerFile.getName());

        }
        try{
            String controllerDocs = controllerDocBuilder.buildDoc(controllerNodeList);
            Utils.writeToDisk(new File(DocContext.getDocPath(), "index.html"),controllerDocs);
            String style=FileUtil.readString(this.getClass().getResource("/template/style.css"),"utf-8");
            Utils.writeToDisk(new File(DocContext.getDocPath(), "style.css"),style);
            ;

        } catch (IOException e) {
            LogUtils.error("generate docs for controller file : index.html fail", e);
        }




    }

    public List<ControllerNode> getControllerNodeList(){
        return controllerNodeList;
    }



    public void generateIndex(List<String> docFileNameList){
        FileUtil.copy("/doc/test/template/","/doc/test/out/",true);
    }
}
