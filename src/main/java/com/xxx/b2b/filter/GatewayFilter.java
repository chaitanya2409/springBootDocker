package com.xxx.b2b.filter;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayFilter extends ZuulFilter {
    @Autowired
    LoadProperties loadProperties;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Value("${unauthorized.url.redirect:https://xyz.com}")
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private String urlRedirect;
    
        /**
     * Indicates which type of Zuul filter this is. In our case, the "route"
     * value is returned to indicate to the Zuul server that this is a route
     * filter.
     *
     * @return "route"
     */
    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * Indicates if a request should be filtered or not. In our case, all
     * requests are taken into account (always return true).
     *
     * @return true
     */
    public boolean shouldFilter() {
        return true;
    }

    /**
     * Indicates if the provided request is authorized or not.
     *
     * @param request
     *            the provided request
     *
     * @return true if the provided request is authorized, false otherwise
     */
    private boolean isAuthorizedRequest(HttpServletRequest request) {
        String smUser = request.getHeader("SM_USER");
        String originValue = request.getHeader("Origin");
        String refererValue = request.getHeader("Referer");
        log.info("GatewayFilter -> isAuthorizeRequest -> Origin header value:" + originValue+" Referer header value: "+ refererValue);
    
        if((null == originValue && null == refererValue)
                ||(originValue != null && !originValue.endsWith("sony.com"))
                || (refererValue != null && !refererValue.contains("sony.com"))) {
            return false;
        }
        log.info("GatewayFilter -> isAuthorizeRequest -> SM_USER header value:" + smUser);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if(path.toLowerCase().contains("nas/") || path.toLowerCase().contains("up/"))
        {
            log.info("GatewayFilter -> isAuthorizeRequest -> Path: "+path);
            return true;
        } else if(StringUtils.isNotBlank(smUser)){
            return true;
        }
        log.info("GatewayFilter -> isAuthorizeRequest -> Path: "+path);
        return false;
    }

    /**
     * The filter execution
     *
     * @return
     */
    public Object run() {

        // logging the interception of the query
        log.info("query interception");

        // retrieving the Zuul request context
        RequestContext ctx = RequestContext.getCurrentContext();

        try {

            
            // Rely on HttpServletRequest to retrieve the correct remote address from upstream X-Forwarded-For header
            HttpServletRequest request = ctx.getRequest();
            String remoteAddr = request.getRemoteAddr();
            log.info("Client RemoteAddr at GatewayServer: "+remoteAddr);
            
            // Pass remote address downstream by setting X-Forwarded for header again on Zuul request
            log.info("Settings X-Forwarded-For to: {}", remoteAddr);
            ctx.getZuulRequestHeaders().put(X_FORWARDED_FOR, remoteAddr);

            // if the requested url is authorized, the route host is set to the
            // requested one
            if (isAuthorizedRequest(ctx.getRequest())) {
            } else {
                // if the requested URL is not authorized, the route host is set
                // to the urlRedirect value
                // the client will be redirected to the new host
                log.info("URL Redirect : "+urlRedirect);
                ctx.setRouteHost(new URL("https://xyz.com"));
            }
        } catch (MalformedURLException e) {
            System.out.println(e);
            log.error("Exception in Gateway filter run(): "+e);
        } catch (Exception e) {
            System.out.println(e);
            log.error("Exception in Gateway filter run(): "+e);
        }

        return null;
    }
    
    
    private String getHostName(){
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }
}
