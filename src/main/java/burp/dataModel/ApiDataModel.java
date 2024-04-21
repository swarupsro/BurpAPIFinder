package burp.dataModel;

import burp.IHttpRequestResponse;
import burp.IHttpService;

import java.util.Map;

/**
 * @author： shaun
 * @create： 2024/4/9 22:12
 * @description：TODO
 */

public class ApiDataModel {
    final String id;
    final String url;
    byte[] requestsData;
    byte[] responseData;
    IHttpService iHttpService;
    String status;
    final String isJsFindUrl;
    final String method;
    String pathNumber;
    Boolean havingImportant;
    String result ;
    String time;
    Map<String, Object> pathData;
    String listStatus;
    String describe;
    String resultInfo;



    public ApiDataModel(String listStatus, String id, String url, String pathNumber, Boolean havingImportant, String result, byte[] requestsData, byte[] responseData, IHttpService iHttpService, String time, String status, String isJsFindUrl, String method, Map<String, Object> pathData, String describe, String resultInfo) {
        this.listStatus = listStatus;
        this.id = id;
        this.url = url;
        this.pathNumber = pathNumber;
        this.havingImportant = havingImportant;
        this.result = result;
        this.requestsData = requestsData;
        this.responseData = responseData;
        this.iHttpService = iHttpService;
        this.time = time;
        this.status = status;
        this.isJsFindUrl = isJsFindUrl;
        this.method = method;
        this.pathData = pathData;
        this.describe = describe;
        this.resultInfo = resultInfo;
    }


    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setResultInfo(String resultInfo) {
        this.resultInfo = resultInfo;
    }

    public String getResultInfo() {
        return resultInfo;
    }

    public byte[] getRequestsData(){
        return this.requestsData;
    }

    public byte[] getResponseData(){
        return this.responseData;
    }

    public IHttpService getiHttpService(){
        return this.iHttpService;
    }

    public String getListStatus(){
        return this.listStatus;
    }

    public void setListStatus(String listStatus){
        this.listStatus = listStatus;
    }


    public String getId(){
        return this.id;
    }

    public String getUrl(){
        return this.url;
    }

    public String getPATHNumber(){
        return this.pathNumber;
    }

    public void setPathNumber(String pathNumber){
        this.pathNumber = pathNumber;
    }

    public String getMethod(){
        return this.method;
    }

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getIsJsFindUrl(){
        return this.isJsFindUrl;
    }

    public Boolean getHavingImportant(){
        return this.havingImportant;
    }

    public void setHavingImportant(Boolean havingImportant){
        this.havingImportant = havingImportant;
    }

    public String getResult(){
        return this.result;
    }

    public void setResult(String result){
        this.result = result;
    }

    public String getTime(){
        return this.time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public Map<String, Object> getPathData() {
        return pathData;
    }

    public void setPathData(Map<String, Object> pathData) {
        this.pathData = pathData;
    }
}