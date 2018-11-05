package com.github.daote.apidoc.codegenerator.provider;

import com.github.daote.apidoc.codegenerator.IFieldProvider;

/**
 * Created by user on 2016/12/25.
 */
public class ProviderFactory {

    public static IFieldProvider createProvider(){
        return new DocFieldProvider();
    }
}
