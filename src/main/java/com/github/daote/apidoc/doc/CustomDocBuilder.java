package com.github.daote.apidoc.doc;

import com.github.daote.apidoc.DocContext;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.github.daote.apidoc.parser.ControllerNode;
import com.github.daote.apidoc.parser.RequestNode;
import com.github.daote.apidoc.codegenerator.code.CustomCshapCodeGenerator;
import com.github.daote.apidoc.codegenerator.code.CustomJavaCodeGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomDocBuilder {

    public String buildDoc(List<ControllerNode> controllerNodes) throws IOException {
        for(ControllerNode controllerNode:controllerNodes){
            for (RequestNode requestNode : controllerNode.getRequestNodes()){
                requestNode.getCodemap().put("java",new ArrayList<>());
                requestNode.getCodemap().put("cshap",new ArrayList<>());
                CustomJavaCodeGenerator.generateCodeForBuilder(requestNode.getResponseNode(),requestNode.getCodemap().get("java"));
                CustomCshapCodeGenerator.generateCodeForBuilder(requestNode.getResponseNode(),requestNode.getCodemap().get("cshap"));
            }
        }
//        Gson gson=new Gson();
//        String ss=gson.toJson(controllerNode);
//        System.out.println(ss);

        String html=Engine.use()
                .setSourceFactory(new ClassPathSourceFactory())
                .setBaseTemplatePath(null)
                .getTemplate("/template/template.html")
                .renderToString(Kv.by("nodes", controllerNodes)
                        .set("now",System.currentTimeMillis())
                        .set("title", DocContext.getDocsConfig().getTitle())
                );
        return html;
    }

    public static void main(String[] args) {

        String html=Engine.use().setBaseTemplatePath("/doc/test/template").getTemplate("index.html").renderToString(Kv.by("hello", "哈哈哈"));
        System.out.println(html);
    }
}
