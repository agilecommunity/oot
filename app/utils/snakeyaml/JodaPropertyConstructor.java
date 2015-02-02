package utils.snakeyaml;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import play.data.format.Formats;
import utils.controller.ParameterConverter;

import java.text.ParseException;
import java.util.Date;

/* https://code.google.com/p/snakeyaml/wiki/howto より */
public class JodaPropertyConstructor extends Constructor {
    public JodaPropertyConstructor() {
        yamlClassConstructors.put(NodeId.scalar, new TimeStampConstruct());
    }

    class TimeStampConstruct extends Constructor.ConstructScalar {
        @Override
        public Object construct(Node nnode) {
            if (nnode.getTag().equals("tag:yaml.org,2002:timestamp")) {
                org.yaml.snakeyaml.nodes.ScalarNode snode = (org.yaml.snakeyaml.nodes.ScalarNode)nnode;
                DateTime value = ParameterConverter.convertTimestampFrom(snode.getValue());
                return value;
            } else {
                return super.construct(nnode);
            }
        }
    }

    @Override
    protected Class<?> getClassForName(String name) throws ClassNotFoundException {
        return Class.forName(name, true, play.Play.application().classloader());
    }
}
