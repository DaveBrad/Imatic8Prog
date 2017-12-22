/* Copyright (c) 2017 dbradley. */
package imatic8;

/**
 * Class that extends a (declared) class within the Imatic8XXXX program structure
 * to provide access to data fields necessary for processing the INI files
 * when performing tests.
 * 
 * @author dbradley
 */
 public class Im8BoardIniExt extends Im8BoardIni{
     Im8BoardIniExt(Im8Io m8Io, int boardN) {
        super(m8Io, boardN);
    }
}
