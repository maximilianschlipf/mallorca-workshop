package de.nordwind.schulungsplaner.katalog;

/** Zu der angegebenen Kennung gibt es keine Schulung im Katalog. */
public class SchulungNichtGefunden extends RuntimeException {

    public SchulungNichtGefunden(SchulungId id) {
        super("Es gibt keine Schulung mit der Kennung '" + id + "'.");
    }
}
