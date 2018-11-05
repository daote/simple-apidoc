package com.github.daote.apidoc.parser;

import java.util.*;

/**
 * a request node  corresponds to a controller method
 *
 * @author yeguozhong yedaxia.github.com
 */
public class RequestNode {

    private List<String> method = new ArrayList<>();
    private String url;
    private String methodName; //方法名
    private String title;
    private String description;
    private List<ParamNode> paramNodes = new ArrayList<>();
    private List<HeaderNode> header = new ArrayList<>();
    private Boolean deprecated = Boolean.FALSE;
    private ResponseNode responseNode;
    private ControllerNode controllerNode;

    private Map<String,List<String>> codemap=new HashMap<>();

    public Map<String, List<String>> getCodemap() {
        return codemap;
    }

    public void setCodemap(Map<String, List<String>> codemap) {
        this.codemap = codemap;
    }

    public List<String> getMethod() {
        if(method == null || (method != null && method.size() == 0)) {
            return Arrays.asList(RequestMethod.GET.name(), RequestMethod.POST.name());
        }
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description !=null)
            description=description.replace("\n","<br/>");
        this.description = description;
    }

    public List<ParamNode> getParamNodes() {
        return paramNodes;
    }

    public void setParamNodes(List<ParamNode> paramNodes) {
        this.paramNodes = paramNodes;
    }

    public List<HeaderNode> getHeader() {
        return header;
    }

    public void setHeader(List<HeaderNode> header) {
        this.header = header;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public ResponseNode getResponseNode() {
        return responseNode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setResponseNode(ResponseNode responseNode) {
        this.responseNode = responseNode;
    }

    public void addMethod(String method) {
        this.method.add(method);
    }

    public void addHeaderNode(HeaderNode headerNode){
        header.add(headerNode);
    }

    public void addParamNode(ParamNode paramNode){
        paramNodes.add(paramNode);
    }

    public ControllerNode getControllerNode() {
        return controllerNode;
    }

    public void setControllerNode(ControllerNode controllerNode) {
        this.controllerNode = controllerNode;
    }

    public ParamNode getParamNodeByName(String name){
        for(ParamNode node : paramNodes){
            if(node.getName().equals(name)){
                return node;
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
