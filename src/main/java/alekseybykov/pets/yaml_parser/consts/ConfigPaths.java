package alekseybykov.pets.yaml_parser.consts;

public enum ConfigPaths {

    CONFIG_FORM(
            String.format(
                    "%s/configs/forms/config.yaml",
                    System.getProperty("user.dir")
            )
    );

    private final String path;

    ConfigPaths(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
