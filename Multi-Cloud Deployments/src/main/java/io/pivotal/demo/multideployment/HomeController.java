package io.pivotal.demo.multideployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Properties;

@Controller
public class HomeController {
    private final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final JsonParser parser = new GsonJsonParser();


    @Value("${isEmbedded}")
    private Boolean isEmbeddedServer;

    @RequestMapping("/")
    public ModelAndView getHome() {
        ModelAndView mav = new ModelAndView("index");

        String envPasGuid = System.getenv("CF_INSTANCE_GUID");

        String propGlassfish = System.getProperty("glassfish.version");
        String propJetty = System.getProperty("jetty.git.hash");
        String propTomcat = System.getProperty("catalina.base");

        String vcapAppString = System.getenv("VCAP_APPLICATION");
        String thisApiUrl = null;

        try {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> log.info(String.format("Key: %s\n\tValue: %s", k, v)));
        } catch (Exception e) {
            log.error("Error Parsing Environment Variables");
            log.error(e.getLocalizedMessage());
        }

        try {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> log.info(String.format("Key: %s\n\tValue: %s", k, v)));
        } catch (Exception e) {
            log.error("Error Parsing JVM Properties");
            log.error(e.getLocalizedMessage());
        }


        try {
            Map<String, Object> stringObjectMap = parser.parseMap(vcapAppString);
            thisApiUrl = stringObjectMap.get("cf_api").toString();
        } catch (Exception e) {
            log.error(String.format("Error extracting CF_API from VCAP_APPLICATION. " +
                    "This may not be running on PAS.\nError: %s", e.getLocalizedMessage()));
        }

        log.info(String.format(
                "\n\tIAAS: %s" +
                        "\n\tCF_INSTANCE_GUID: %s" +
                        "\n\tGLASSFISH_VERSION: %s" +
                        "\n\tJETTY_GIT_HASH: %s" +
                        "\n\tCATALINA_BASE: %s",
                resolveIaasFromUrl(thisApiUrl), envPasGuid, propGlassfish, propJetty, propTomcat));

        mav.addObject("isJetty", propJetty != null);
        mav.addObject("isGlassfish", propGlassfish != null);
        mav.addObject("isTomcat", (propTomcat != null && propGlassfish == null));
        mav.addObject("isEmbedded", isEmbeddedServer);
        mav.addObject("isPas", envPasGuid != null);
        mav.addObject("iaas", resolveIaasFromUrl(thisApiUrl));

        return mav;
    }

    private String resolveIaasFromUrl(String apiUrl) {
        if(apiUrl == null){
            return "physical";
        }

        String resolvedIaas;
        switch (apiUrl) {
            case "https://api.run.pivotal.io":
                resolvedIaas = "pws";
                break;
            case "https://api.sys.mcnichol.rocks":
                resolvedIaas = "aws";
                break;
            case "https://api.sys.lab.local":
                resolvedIaas = "vsphere";
                break;
            default:
                resolvedIaas = "physical";
        }

        return resolvedIaas;
    }
}