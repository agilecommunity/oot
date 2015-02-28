package utils.snakeyaml;

import org.joda.time.DateTime;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import utils.controller.parameters.ParameterConverter;

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
