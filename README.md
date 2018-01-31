A Java JAR program that will drive an 8 channel 5V relay board via an
Imatic control board. (The project was Netbeans 8.2 created.)

Supports N boards for boards that have their IP changed.

     Usage: - Command-line mode = 'Imatic8Prog.jar [args]'
               'Imatic8Prog.jar on 1 2 ms:500 on 3 s:10 off 1 s:2 off 2 b-2 on 1'
      or    - Interactive mode  = 'Imatic8Prog.jar'
               I>[ args | exit | quit | q]     [eg. I>'on 1 2 ms:500 on 3 ] b-2 on 4' | 'exit'
               I> ---> next
     [args...]
        help | -help | /? | ? | license | l
      - setup -
        defIP-N nnn.nnn.nnn.nnn         ( define an IP address to associate with board-N )
        defIP                           ( query the defined board-N to IP addresses  )
      - operations -
        b-N                             ( set board N, ~ no b-N defaults to 'b-1' ~)
        on n [n [n...]]] | on all       ( on relays, b-1 if no preceding b-N )
        off n [n [n...]]] | off all     ( off relays, b-1 if no preceding b-N )
        s:N | ms:N                      ( pause N seconds/milliseconds )
        status                          ( 'Status:b-1:12--5---'    b-N=board-N  digit=ON
                                            b-1 if no preceding b-N
                                           > board has no query, so best guess status <  )

The Imatic8 board does not support a relay status query, so this program 
simulates a best guess 'status' via an INI file. This implies any power-loss 
to the board results in out-of-sync states between the board and INI states.

The JAR file may be used as a "library".

If you wish to run the JAR file, download just the jarDL/Imatic8Prog.jar from
the repository. To run (if no JAR file association) with:
    'java -jar Imatic8Prog.jar' or  'java -jar Imatic8Prog.jar [args]'