package com.github.daote.apidoc.codegenerator.provider;


import com.github.daote.apidoc.codegenerator.IFieldProvider;
import com.github.daote.apidoc.codegenerator.model.FieldModel;
import com.github.daote.apidoc.parser.FieldNode;
import com.github.daote.apidoc.parser.ResponseNode;
import com.github.daote.apidoc.Utils;

import java.util.ArrayList;

import java.util.List;

public class DocFieldProvider implements IFieldProvider {

	@Override
	public List<FieldModel> provideFields(ResponseNode respNode) {
		List<FieldNode>recordNodes = respNode.getChildNodes();
		if(recordNodes == null || recordNodes.isEmpty()){
			return null;
		}
		List<FieldModel> entryFieldList = new ArrayList<>();
		FieldModel entryField;
		for (FieldNode recordNode : recordNodes) {
			entryField = new FieldModel();
			String fieldName = DocFieldHelper.getPrefFieldName(recordNode.getName());
			entryField.setCaseFieldName(Utils.capitalize(fieldName));
			entryField.setFieldName(fieldName);
			entryField.setFieldType(DocFieldHelper.getPrefFieldType(recordNode.getType()));
			entryField.setRemoteFieldName(recordNode.getName());
			entryField.setComment(recordNode.getDescription());
			entryFieldList.add(entryField);
		}
		return entryFieldList;
	}
	
}
