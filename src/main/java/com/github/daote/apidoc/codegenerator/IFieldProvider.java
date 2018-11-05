package com.github.daote.apidoc.codegenerator;

import com.github.daote.apidoc.parser.ResponseNode;
import com.github.daote.apidoc.codegenerator.model.FieldModel;

import java.util.List;

public interface IFieldProvider {
	/**
	 * get response fields
	 * @param respNode
	 * @return
	 */
	List<FieldModel> provideFields(ResponseNode respNode);
}
