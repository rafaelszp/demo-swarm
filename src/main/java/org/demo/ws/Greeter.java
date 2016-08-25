package org.demo.ws;

import org.demo.model.Salutation;

import javax.jws.WebService;

/**
 * Created by rafael on 8/22/16.
 */
@WebService
public interface Greeter {
    public String greetMe(Salutation salutation);
    public String sayHi();
    public String willBreak();

}
