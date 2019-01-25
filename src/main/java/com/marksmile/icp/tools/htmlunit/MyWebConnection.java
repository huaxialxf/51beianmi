package com.marksmile.icp.tools.htmlunit;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class MyWebConnection extends FalsifyingWebConnection {

    private static Logger logger = LoggerFactory.getLogger(MyWebConnection.class);

    public WebResponse getResponse(WebRequest request) throws IOException {

        WebResponse response = super.getResponse(request);
        if("/icp/publish/query/icpMemoInfo_showPage.action".equals(request.getUrl().getPath())){
            Set<Cookie> cookies = this.webClient.getCookieManager().getCookies();
            StringBuilder builder = new StringBuilder();
            for (Cookie cookie : cookies) {
//                logger.info("{}:{}",cookie.getName(),cookie.getValue());
                builder.append(String.format("%s:%s", cookie.getName(),cookie.getValue()));
            }
            if(cookies.size()>1){
                return new StringWebResponse(builder.toString(), request.getUrl());
            }
        }
        
        // request.removeAdditionalHeader("Referer");
//        if ("/".equals(request.getUrl().getPath())) {
//            System.out.println("index>>>>>>>>>>>>>>>>>>\n" + response.getContentAsString());
//            String html ="<!DOCTYPE html><html><head><title>test</title></head><body ></body></html>";
//            response = replaceContent(response, html);
//            System.out.println("index>>>>>>>>>>>>>>>>>>\n" + response.getContentAsString());
//        }
        // if (request.getUrl().getPath().startsWith("/txnS02.do")) {
        // System.out.println("txnS02.do>>>>>>>>>>>>>>>>>>\n" +
        // response.getContentAsString());
        // }

        return response;
    }

    protected WebResponse replaceContent(final WebResponse wr, final String newContent) throws IOException {
        final byte[] body = newContent.getBytes(wr.getContentCharset());
        List<NameValuePair>  list = wr.getResponseHeaders();
        List<NameValuePair>  listNew = new ArrayList<NameValuePair>();
        for (NameValuePair nameValuePair : list) {
            if(nameValuePair.getName().equalsIgnoreCase("Content-Encoding")){
                continue ;
            }
            listNew.add(nameValuePair);
        }
        final WebResponseData wrd = new WebResponseData(body, wr.getStatusCode(), wr.getStatusMessage(),listNew);
        return new WebResponse(wrd, wr.getWebRequest().getUrl(), wr.getWebRequest().getHttpMethod(), wr.getLoadTime());
    }
    private WebClient webClient;
    public MyWebConnection(WebClient webClient) throws IllegalArgumentException {
        super(webClient);
        this.webClient =webClient ;
    }
}
