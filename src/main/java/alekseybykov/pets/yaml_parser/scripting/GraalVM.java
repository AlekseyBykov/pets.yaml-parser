package alekseybykov.pets.yaml_parser.scripting;

import org.graalvm.polyglot.Context;

public class GraalVM {

    public static Context newJSContext() {
        return Context.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .allowAllAccess(true)
                .build();
    }
}
