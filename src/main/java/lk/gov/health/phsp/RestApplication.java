package lk.gov.health.phsp;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application Configuration for REST API
 * Configures Jersey with Jackson for JSON processing and multipart file upload support
 *
 * @author Dr M H B Ariyaratne
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Register REST controllers
        classes.add(lk.gov.health.phsp.bean.RestAuthenticationController.class);
        classes.add(lk.gov.health.phsp.bean.RestQrScanController.class);

        // Register Jackson JSON provider
        classes.add(org.glassfish.jersey.jackson.JacksonFeature.class);

        // Register MultiPart feature for file upload support
        classes.add(org.glassfish.jersey.media.multipart.MultiPartFeature.class);

        return classes;
    }
}
