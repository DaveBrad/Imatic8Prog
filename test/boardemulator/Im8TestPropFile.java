/* Copyright (c) 2017 dbradley. */
package boardemulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author dbradley
 */
class Im8TestPropFile extends Properties {

     Im8TestPropFile() {
    }

    void load() {

    }
    
    void store(){
        
    }
    
       static void createEmulatePropFile() {
        // set the properties for activation of the emulator test mode
        // - done by a shared file within the user.dir for any running
        //   environment
        Properties emulatorProp = new Properties();
        emulatorProp.setProperty("emulate4test", Im8TestShadowBoardSvr.class.getName());

        File propFile = new File(System.getProperty("user.dir"), "Emulator.prop");

        propFile.deleteOnExit();

        FileOutputStream oStream = null;
        try {
            oStream = new FileOutputStream(propFile);
            try {
                emulatorProp.store(oStream, "Imatic8 board emulator");

            } catch (IOException ex1) {
                throw new RuntimeException("Failure to create emulator for testing.", ex1.getCause());
            } finally {
                if (oStream != null) {
                    try {
                        oStream.close();
                    } catch (IOException ex) {
                        // nothing we can do
                    }
                }
            }

        } catch (FileNotFoundException ex1) {
            throw new RuntimeException("Failure to create emulator for testing.");
        }

    }

}
