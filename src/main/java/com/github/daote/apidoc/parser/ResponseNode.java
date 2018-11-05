package com.github.daote.apidoc.parser;

/**
 * response node
 *
 * @author yeguozhong yedaxia.github.com
 */
public class ResponseNode extends ClassNode{

    private RequestNode requestNode;

    public RequestNode getRequestNode() {
        return requestNode;
    }

    public void setRequestNode(RequestNode requestNode) {
        this.requestNode = requestNode;
    }
}
