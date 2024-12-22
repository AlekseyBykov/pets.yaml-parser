package alekseybykov.pets.yaml_parser;

import alekseybykov.pets.yaml_parser.consts.ConfigPaths;
import alekseybykov.pets.yaml_parser.mappers.JacksonMapper;
import alekseybykov.pets.yaml_parser.models.Config;
import alekseybykov.pets.yaml_parser.scripting.GraalVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("config-tests")
public class ConfigTest {

    @Nested
    final class ContentTest {
        @Test
        void checkConfigStructure() throws Exception {
            ObjectMapper mapper = JacksonMapper.newMapper();

            var form = mapper.readValue(
                    new File(ConfigPaths.CONFIG_FORM.getPath()),
                    Config.class
            );

            var data = form.getData();
            assertNotNull(data);

            var properties = data.getProperties();
            assertNotNull(properties);

            var property = properties.getFirst();
            assertNotNull(property);

            var expressions = property.getExpressions();
            assertNotNull(expressions);

            assertEquals(
                    """
                            status !== 'NEW' \
                            || !['A', 'B', 'C', 'D'].includes(type) \
                            || (type === 'A' && sign?.includes('63')) \
                            || org !== 'org'""",
                    expressions.getFirst().getExpression()
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    final class ExpressionTest {

        private static final String LANGUAGE_ID = "js";
        private static final String ACTIVATING_EXPRESSION = """
                function isActive(
                    status,
                    type,
                    sign,
                    org
                ) {
                    /*print(type === 'NEW');*/
                    return status !== 'NEW'
                        || !['A', 'B', 'C', 'D'].includes(type)
                        || (type === 'A' && sign?.includes('63'))
                        || org !== 'org';
                }""";

        private Context jsContext;

        @BeforeAll
        void setUp() {
            jsContext = GraalVM.newJSContext();
        }

        @AfterAll
        void tearDown() {
            jsContext.close();
        }

        @Test
        void isD017ButtonDisabled() {
            jsContext.eval(
                    LANGUAGE_ID,
                    ACTIVATING_EXPRESSION
            );

            Value function = jsContext.getBindings(LANGUAGE_ID).getMember("isActive");

            assertAll(
                    () -> assertFalse(function.execute("NEW", "B", "63", "org").asBoolean()),
                    () -> assertFalse(function.execute("NEW", "C", "63", "org").asBoolean()),
                    () -> assertFalse(function.execute("NEW", "D", "63", "org").asBoolean()),
                    () -> assertFalse(function.execute("NEW", "B", "63", "org").asBoolean()),
                    () -> assertFalse(function.execute("NEW", "A", "00", "org").asBoolean()),

                    () -> assertTrue(function.execute("NEW", "A", "63", "org").asBoolean()),
                    () -> assertTrue(function.execute("ANY", "A", "63", "org").asBoolean()),
                    () -> assertTrue(function.execute("ANY", "X", "63", "org").asBoolean())
            );
        }
    }
}
