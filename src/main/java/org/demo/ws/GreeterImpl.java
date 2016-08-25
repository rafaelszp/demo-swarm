package org.demo.ws;

import org.demo.model.Salutation;

import javax.jws.WebService;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Created by rafael on 8/22/16.
 */
@WebService(serviceName = "GreeterService",
        endpointInterface = "org.demo.ws.Greeter",
        targetNamespace = "http://webservice.demo.org/GreeterService",
        portName="Greeter"
)
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)

public class GreeterImpl implements Greeter {

    @PersistenceContext
    EntityManager em;

    @Override
    public String greetMe(Salutation salut) {
        return "Hello "+salut.getSurName()+" "+salut.getPersonName();
    }

    @Override
    public String sayHi() {
        em.createNativeQuery("select 1").getFirstResult();
        return "Hi";
    }

    @Override
    public String willBreak() {
        throw new RuntimeException("Broke successfully!");
    }
}
