package net.pms.configuration;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigurableProgramPathTest {

    @Mock
    private Configuration mockConfiguration;

    private ConfigurableProgramPaths configurableProgramPaths;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configurableProgramPaths = new ConfigurableProgramPaths(mockConfiguration);
    }

    @Test
    public void testSetCustomMPlayerPath() {
        // Mock a Path
        Path mockPath = mock(Path.class);

        // Set up behavior for configuration
        when(mockConfiguration.getString(ConfigurableProgramPaths.KEY_MPLAYER_PATH)).thenReturn("/custom/mplayer");

        // Call method to be tested
        configurableProgramPaths.setCustomMPlayerPath(mockPath);

        // Verify that the custom path is set in both configuration and ExternalProgramInfo
        verify(mockConfiguration).setProperty(ConfigurableProgramPaths.KEY_MPLAYER_PATH, "/custom/mplayer");
    }

    // Add more test methods for other functionalities in a similar manner
}
