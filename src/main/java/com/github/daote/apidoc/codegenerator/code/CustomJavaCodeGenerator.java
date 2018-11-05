package com.github.daote.apidoc.codegenerator.code;

import com.github.daote.apidoc.codegenerator.IFieldProvider;
import com.github.daote.apidoc.codegenerator.model.FieldModel;
import com.github.daote.apidoc.codegenerator.provider.ProviderFactory;
import com.github.daote.apidoc.parser.ClassNode;
import com.github.daote.apidoc.parser.FieldNode;
import com.github.daote.apidoc.parser.ResponseNode;

import java.io.IOException;
import java.util.List;

public class CustomJavaCodeGenerator {

    public static void generateCodeForBuilder(ClassNode rootNode, List<String> codes) throws IOException{
        codes.add(generateNodeCode((ResponseNode) rootNode));
        for (FieldNode recordNode : rootNode.getChildNodes()) {
            if (recordNode.getChildNode()!= null) {
                generateCodeForBuilder(recordNode.getChildNode(), codes);
            }
        }
    }

    public static String generateNodeCode(ResponseNode respNode) throws IOException {
        String className = respNode.getClassName();
        IFieldProvider entryProvider = ProviderFactory.createProvider();
        List<FieldModel> entryFields = entryProvider.provideFields(respNode);
        if(entryFields == null || entryFields.isEmpty()){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class ").append(className).append("{\n");

        for (FieldModel entryFieldModel : entryFields) {
            stringBuilder.append("\tprivate ")
                    .append(entryFieldModel.getFieldType().contains("[")?"List<"+entryFieldModel.getFieldType().split("\\[")[0]+">":entryFieldModel.getFieldType())
                    .append(" ")
                    .append(entryFieldModel.getFieldName())
                    .append(";  //")
                    .append(entryFieldModel.getComment())
                    .append("\n");
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

}
