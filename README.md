A Java JAR program that will drive an 8 channel 5V relay board via an
Imatic control board. (The project was Netbeans 8.2 created.)

Usage: - Command-line mode 
    eg.    Imatic8Prog.jar on 1 2 ms:500 on 3 s:10 off 1 s:2 off 2 3
 or    - Interactive mode
    eg.    Imatic8Prog.jar
           I>on 1 2 ms:500 on 3 s:10 off 1 s:2 off 2 3
[args...]
   help | -help | /? | ? | license | l   [ exit | quit | q   - interactive only -]
 - operations -
   on n [n [n...]]] | on all       (on relays)
   off n [n [n...]]] | off all     (off relays)
   s:N | ms:N                      (pause N seconds/milliseconds)
   status                          ( 'Status:12--5---'                digit=ON      )
                                   (  > board has no query, so best guess status <  }


The Imatic8 board does not support a relay status query, so this program 
simulates a best guess 'status' via an INI file. This implies any power-loss 
to the board results in out-of-sync states between the board and INI states.

The JAR file may be used as a "library".

If wish to run just the JAR file, download the dist/Imatic8Prog.jar from
from the repository. To run (if no JAR file association with:
'java -jar Imatic8Prog.jar'  or 'java -jar Imatic8Prog.jar [args]').