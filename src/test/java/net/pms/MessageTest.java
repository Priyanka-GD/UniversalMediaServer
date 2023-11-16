package net.pms;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessagesTest {

    @Test
    void testGetString() {
        // Test basic string retrieval
        String localizedString = Messages.getString("some_key");
        assertNotNull(localizedString);
        // Replace "some_key" with an existing key from the properties file
        assertEquals("Expected localized string", localizedString);
        
        // Test string retrieval for a specific locale
        Locale specificLocale = new Locale("en", "US"); // Replace with the locale you want to test
        String localizedStringForLocale = Messages.getString("some_key", specificLocale);
        assertNotNull(localizedStringForLocale);
        // Replace "some_key" with an existing key from the properties file
        assertEquals("Expected localized string for the specific locale", localizedStringForLocale);
        
        // Test string retrieval for the root language file
        String rootLocalizedString = Messages.getRootString("some_key");
        assertNotNull(rootLocalizedString);
        // Replace "some_key" with an existing key from the properties file
        assertEquals("Expected root localized string", rootLocalizedString);
    }

    // Add more test cases as needed to cover other methods in the Messages class
}
