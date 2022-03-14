package gpw.pl.tsi.utils;

import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

@Component
public class JaxbUtils {

    public String getAsString(Object response){
        String responseString = "";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(response.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter out = new StringWriter();
            marshaller.marshal(response, out);
            responseString = System.lineSeparator() + out.toString();
        } catch (JAXBException e) {
            responseString = "Error while converting response to String.";
            e.printStackTrace();
        }
        return responseString;
    }
}
