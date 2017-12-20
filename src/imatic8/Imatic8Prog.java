/*
 * Copyright (c) 2017 dbradley.
 *
 * License: Imatic8Prog
 *
 * Free to use software and associated documentation (the "Software")
 * without charge.
 *
 * Distribution, merge into other programs, copy of the software is
 * permitted with the following a) to c) conditions:
 *
 * a) Software is provided as-is and without warranty of any kind. The user is
 * responsible to ensure the "software" fits their needs. In no event shall the
 * author(s) or copyholder be liable for any claim, damages or other liability
 * in connection with the "Software".
 *
 * b) Permission is hereby granted to modify the "Software" with two sub-conditions:
 *
 * b.1) A 'Copyright (c) <year> <copyright-holder>.' is added above the original
 * copyright line(s).
 *
 * b.2) The Main class name is changed to identify a different "program" name
 * from the original.
 *
 * c) The above copyright notice and this permission/license notice shall
 * be included in all copies or substantial portions of the Software.
 */
package imatic8;

/**
 * Program (main) that will interact with a Version 1.1 Imatic8 board to set
 * relays on or off on a compatible 8-channel 5V relay board&#46; The Imatic8
 * board does not support a relay status query, so this program simulates a best
 * guess 'status' via an INI file.
 * <p>
 * The Imatic8 control and relay board are separate boards that connect to allow
 * relays to be controlled. This program allows a user to control the relays via
 * command-line mode or in an interactive mode (or as java library). As a Java
 * application, provides OS platform independent operation of the relays. (Most
 * other S/W is C, C++ which needs to be compiled for specific OS platforms,
 * this Java program requires an installed JRE arrangement.)
 * <p>
 * IMPORTANT: The Imatic8 board lacks a means to query the relay states so this
 * program records the states as a best guest. It will be out of sync if there
 * are power outages, disconnects of either board or this controlling program.
 * <h1>Usage</h1>
 * <table class="mode">
 * <caption>operation modes</caption>
 * <tr>
 * <th style="background: #e6ffff;">Command line mode</th>
 * <th  style="background: #c6f3ed;">Interactive mode</th>
 * <th style="background: #ffffcc;">Library mode (Java)</th>
 * <tr>
 * <td style="background: #e6ffff;">
 * <pre>
 * Imatic8Prog.jar [arguments]&nbsp;
 * </pre>
 * </td>
 * <td style="background: #c6f3ed">
 * <pre>
 * Imatic8Prog.jar&nbsp;
 * I&gt;[arguments]
 * </pre>
 * </td>
 * <td style="background: #ffffcc;">
 * <pre>
 Imatic8IoIf myMatic8 
         = new Imatic8IoIf((new String[]{arg0, arg1, arg2.....);
 myMatic8.runAsLib();
 </pre>
 * <p style="font-size: 0.85em;"> See <i>{@link imatic8.Im8Io}</i>        
 * </p>
 * </td>
 * </tr></table>
 * <p>
 * The [arguments] represent action(s) for processing relay on or off states,
 * with the ability to group actions, delay an action.
 * <table >
 * <caption>[arguments] (case insensitive)</caption>
 * <tr>
 * <td style="vertical-align:top; width: 17ch;">single relay</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">ON 1</span>
 * <span style="color: magenta;">OFF 1</span>
 * </pre>
 * </td>
 * <td style="vertical-align:top"><pre>
 *ON n | off n
 *on n | OFF n
 * </pre>
 * </td>
 * </tr>
 *
 * <tr>
 * <td style="vertical-align:top">grouped relays</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1 8 7</span>
 * <span style="color: magenta;">off 2 3 5</span>
 * </pre>
 * </td>
 * <td style="vertical-align:top">on n [n [n...]]] | off n [n[n...]]]
 * </td>
 * </tr>
 *
 * <tr>
 * <td style="vertical-align:top">mix and match configuration</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1 8</span> <span style="color: magenta;">off 2 3 5</span>
 * <span style="color: green;">on 1</span> <span style="color: magenta;">off 2 3</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 5</span></pre>
 * </td>
 * <td style="vertical-align:top">mixed allows on or off in different ways
 * </td>
 * </tr>
 *
 * <tr>
 * <td style="vertical-align:top">board context</td>
 * <td style="vertical-align:top">
 * <pre> <span style="background: springgreen;">b-1</span> <span style="color: green;">on 1 8</span> <span style="color: magenta;">off 2 3 5</span>
 * <span style="background: springgreen;">b-2,</span> <span style="color: green;">on 1</span> <span style="background: springgreen;">b-1</span> <span style="color: magenta;">off 2 3</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 5</span></pre>
 * </td>
 * <td style="vertical-align:top">for multiple boards the on/off/status needs to
 * be preceded by a 'b-N' operation to set the context, if no b-N is provided on
 * the argument line b-1 is the default.
 * <div style="font-size: 0.75em; margin-left: 15px;">See defip-N for more
 * detail</div>
 * <br>
 * </td>
 * </tr>
 *
 * <tr>
 * <td style="vertical-align:top">pause between operations</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1</span> <span style="background: lime;font-style: bold;">s:10</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 2 3 5</span>
 * <span style="color: green;">on 1</span> <span style="color: magenta;">off 2</span> <span style="background: lime;font-style: bold;">ms:100</span> <span style="color: magenta;">off 3</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 5</span></pre>
 * </td>
 * <td style="vertical-align:top">
 * s:N is N seconds pause, ms:N is N milliseconds pause<br>
 * <div style="font-size: 0.75em; margin-left: 15px;">Note: time is approximate
 * as the time needed to operate physical should be added (so best time).</div>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">best guess relay status query</td>
 * <td style="vertical-align:top"><pre> status</pre></td>
 * <td style="vertical-align:top"><pre style="margin-bottom: 3px;">Status:b-1:123-5---</pre>
 * <div style="font-size: 0.75em; margin-left: 15px;">b-N=board-N<br>ON=digit
 * (is output to the 'System.out' stream)</div>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">help/license</td>
 * <td style="vertical-align:top"><pre> help | -help | /? | ? | license | l
 * </pre></td>
 * <td style="vertical-align:top"> <td>
 * </tr>
 * <tr>
 * <td style="background: #c6f3ed;vertical-align:top">interactive exit</td>
 * <td style="background: #c6f3ed;vertical-align:top"><pre> exit | quit | q</pre></td>
 * <td style="background: #c6f3ed;vertical-align:top"><i>only needed in
 * interactive mode</i></td>
 * </tr>
 * </table>
 * <br><br>
 * <table>
 * <caption style="color: white;">define board</caption>
 * <tr>
 * <td style="background: #ffffe6; vertical-align:top; width: 17ch;">define
 * board N<br>IP address</td>
 * <td style="background: #ffffe6; vertical-align:top">
 * <pre> <span style="color: green;">defip-2 192.168.1.5</span>
 * <span style="color: magenta;">defip-1 192.168.1.8</span>
 * </pre>
 * </td>
 *
 * <td style="background: #ffffe6; vertical-align:top"><pre style="margin-bottom: 0px;">
 * defip-N nnn.nnn.nnn.nnn</pre>
 * <div style="font-size: 0.75em; margin-left: 15px; margin-top: 6px;">'N' board
 * number&nbsp;&nbsp;&nbsp; 'nnn' 0-255 IPV4 number</div>
 * </td>
 * </tr>
 * 
 * <tr>
 * <td style="background: #ffffe6; vertical-align:top; width: 17ch;">query defined<br>board all N</td>
 * <td style="background: #ffffe6; vertical-align:top">
 * <pre> <span style="color: green;">defip</span>
 * </pre>
 * </td>
 *
 * <td style="background: #ffffe6; vertical-align:top"><pre style="margin-bottom: 0px;">
 *b-1 : 192.168.1.4:30000
 *b-2 : 192.168.1.4:30000
 *b-10 : 192.168.1.5:30000</pre>
 * </td>
 * </tr>
 * 
 * <tr>
 * <td style="background: #ffffe6; vertical-align:top"></td>
 * <td style="background: #ffffe6; vertical-align:top" colspan="2">
 * Defines the IP address associated with a 'b-N' operation for board context.
 * ONLY NEEDS TO BE DONE ONCE for each board.
 * <p>
 * The Imatic board can be changed to use a different IP address from the
 * default 192.168.1.4; so setting a board-N allows Imatic8Prog to support a
 * different IP address.
 * <p>
 * If no 'defip-N' is set up, an INI for board-1 will be created using the
 * default IP address.
 * </p>
 * <p>
 * If you have multiple boards on your network (appears to be possible) but with
 * different IP address then a defip-N needs to be done for each board.
 * </p>
 * <P>
 * IMPORTANT: if you wish to change a board-N IP address, delete the INI file
 * and then re-define.
 * <br>
 * <span style="color: magenta;">NOTE:</span> defip-1 192.168.1.8 overrides the
 * default setting IP address but needs to done before using Imatic8Prog.
 * </td>
 * </tr>
 * </table>
 *
 * @author dbradley
 */
public class Imatic8Prog {
    
    /**
     * Change this variable to have the HELP text change automatically.
     * <p>
     * The Javadoc will need to be changed to the changed program-name.
     */
    public final static String programName = "Imatic8Prog";

    private Imatic8Prog() {
        //
    }

    /**
     * Program main entry.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // interactive mode
            Im8ModeInteractive.processInteractiveMode();
        } else {
            // command line mode
            Im8ModeCmdLine.processCmdLineMode(args);
        }
    }
}
