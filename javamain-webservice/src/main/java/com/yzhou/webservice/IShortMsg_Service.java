package com.yzhou.webservice;

import com.sms.client.IShortMsg;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * <p>
 * An example of how this class may be used:
 *
 * <pre>
 * IShortMsg service = new IShortMsg();
 * IShortMsg portType = service.getShortMsgServerPort();
 * portType.downsms(...);
 * </pre>
 *
 * </p>
 *
 */
@WebServiceClient(name = "IShortMsg", targetNamespace = "http://Server_15.sevice.uxunplat.uxun.com/",
        wsdlLocation = "http://172.17.48.230:33327/smspre/ShortMsg?wsdl")
public class IShortMsg_Service extends Service {

    private final static URL ISHORTMSG_WSDL_LOCATION;
    private final static Logger logger = Logger
            .getLogger(IShortMsg_Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = IShortMsg_Service.class.getResource(".");
            url = new URL(baseUrl, "http://172.17.48.230:33327/smspre/ShortMsg?wsdl");
        } catch (MalformedURLException e) {
            logger
                    .warning("Failed to create URL for the wsdl Location: 'http://172.17.48.230:33327/smspre/ShortMsg?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        ISHORTMSG_WSDL_LOCATION = url;
    }

    public IShortMsg_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public IShortMsg_Service() {
        super(ISHORTMSG_WSDL_LOCATION, new QName(
                "http://Server_15.sevice.uxunplat.uxun.com/", "IShortMsg"));
    }

    /**
     *
     * @return returns IShortMsg
     */
    @WebEndpoint(name = "ShortMsgServerPort")
    public IShortMsg getShortMsgServerPort() {
        return super.getPort(new QName(
                "http://Server_15.sevice.uxunplat.uxun.com/",
                "ShortMsgServerPort"), IShortMsg.class);
    }

}
