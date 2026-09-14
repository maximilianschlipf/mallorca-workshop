package de.nordwind.schulungsplaner.katalog.ablage;

import de.nordwind.schulungsplaner.katalog.SchulungId;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Das JSON-Format des Katalogs.
 *
 * <p>Die Dateien liegen versioniert im Repository, werden von Menschen
 * gelesen und im Diff verglichen. Deshalb sind Einrueckung, Feldreihenfolge
 * und Zeilenende hier festgelegt statt der Standardeinstellung ueberlassen:
 * ein Zeilenumbruch ist immer {@code \n}, auch unter Windows -- sonst
 * erzeugte dieselbe Aenderung je nach Rechner einen anderen Diff.
 *
 * <p>{@link SchulungId} wird hier an Jackson angebunden, nicht ueber
 * Annotationen am Typ selbst. So bleibt der Kern des Katalogs frei von
 * Abhaengigkeiten zum Serialisierungswerkzeug.
 */
final class KatalogJson {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .addModule(new SimpleModule("Katalog")
                    .addSerializer(SchulungId.class, new SchulungIdSerializer())
                    .addDeserializer(SchulungId.class, new SchulungIdDeserializer()))
            .build();

    private static final ObjectWriter WRITER = MAPPER.writer().with(
            new DefaultPrettyPrinter().withObjectIndenter(
                    new DefaultIndenter("  ", "\n")));

    private KatalogJson() {
    }

    static JsonMapper mapper() {
        return MAPPER;
    }

    /** Liefert den Dateiinhalt einschliesslich abschliessendem Zeilenumbruch. */
    static String schreibe(Object wert) {
        return WRITER.writeValueAsString(wert) + "\n";
    }

    private static final class SchulungIdSerializer extends ValueSerializer<SchulungId> {
        @Override
        public void serialize(SchulungId id, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeString(id.wert());
        }
    }

    private static final class SchulungIdDeserializer extends ValueDeserializer<SchulungId> {
        @Override
        public SchulungId deserialize(JsonParser p, DeserializationContext ctxt) {
            return SchulungId.von(p.getString());
        }
    }
}
