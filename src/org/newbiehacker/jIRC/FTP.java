import java.io.*;
import java.net.Socket;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 12-Nov-2006</p>
 * <p>Time: 12:15:15</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class FTP {
    private static boolean DEBUG = false;
    private DataOutputStream transfererOut;
    private DataInputStream transfererIn;
    private DataInputStream in;
    private DataOutputStream out;


    public FTP() {
    }

    public final void connect(String host, int port, String user, String pass) throws IOException {
        Socket s = new Socket(host, port);
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());
        if (!read().startsWith("220"))
            throw new RuntimeException("Not allowed to connect to this server!");
        sendRawLine("USER " + user);
        if (!read().startsWith("331"))
            throw new RuntimeException("Invalid username!");
        sendRawLine("PASS " + pass);
        if (!read().startsWith("230"))
            throw new RuntimeException("Invalid password!");
        debug("Connected and logged in.");
    }

    public final void connect(String host, int port) throws IOException {
        connect(host, port, "I.wont@tell.you", "");
    }

    public final void connect(String host) throws IOException {
        connect(host, 21);
    }

    public final void cdup() throws IOException {
        sendRawLine("CDUP");
        if (!read().startsWith("250"))
            throw new RuntimeException("Cannot change to parent dir!");
    }

    public final void cwd(String dir) throws IOException {
        sendRawLine("CWD " + dir);
        if (!read().startsWith("250"))
            throw new RuntimeException("Invalid directory!");
    }

    public final void logout() throws IOException {
        sendRawLine("QUIT");
        debug("Logged out.");
        in = null;
        out = null;
        transfererIn = null;
        transfererOut = null;
    }

    public final void pasv() throws IOException {
        sendRawLine("PASV");
        String s = read();
        if (!s.startsWith("227"))
            throw new RuntimeException("Could not enter passive mode!");
        String data = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
        String[] sub = data.split(",");
        String address = sub[0] + "." + sub[1] + "." + sub[2] + "." + sub[3];
        int port = Integer.parseInt(sub[4]) * 256 + Integer.parseInt(sub[5]);
        debug("Passive mode accepted at " + address + ", port " + port);
        Socket socks = new Socket(address, port);
        transfererOut = new DataOutputStream(socks.getOutputStream());
        transfererIn = new DataInputStream(socks.getInputStream());
    }

    public final File retr(String in, String out) throws IOException {
        pasv();
        File o = new File(out);
        int size = size(in);
        sendRawLine("RETR " + in);
        if (!read().startsWith("150"))
            throw new IOException("Cannot retrieve file!");
        if (!o.createNewFile() || !o.canWrite())
            throw new IOException("Can't create/modify file " + out);
        byte[] data = new byte[size];
        transfererIn.readFully(data);
        transfererIn.close();
        transfererIn = null;
        transfererOut = null;
        new FileOutputStream(o).write(data);
        return o;
    }

    public final void stor(File f) throws IOException {
        pasv();
        if (!f.exists() || !f.canRead())
            throw new IOException("File does not exist or it is not readable!");
        sendRawLine("STOR " + f.getName());
        if (!read().startsWith("150") && !read().startsWith("226"))
            throw new IOException("Error in STOR, either you have not called pasv() or the server does not want to create this file");
        byte[] data = new byte[(int) f.length()];
        new DataInputStream(new FileInputStream(f)).readFully(data);
        transfererOut.write(data);
        transfererOut.flush();
        transfererOut.close();
        transfererOut = null;
        transfererIn = null;
        if (!read().startsWith("226"))
            throw new IOException("Error transferring file!");
    }

    public final void delete(String file) throws IOException {
        sendRawLine("DELE " + file);
        read();
    }

    public final String[] list() throws IOException {
        pasv();
        sendRawLine("LIST");
        if (!read().startsWith("150"))
            throw new RuntimeException("Error in LIST, either you did not call PASV previously or the file/dir does not exist");
        try {
            while (transfererIn.available() < 1) Thread.sleep(100);
        } catch (InterruptedException e) {/**/}
        byte[] data = new byte[transfererIn.available()];
        transfererIn.readFully(data);
        debug(new String(data));
        return new String(data).split("\r\n");
    }

    public final String[] nameList() throws IOException {
        sendRawLine("NLST");
        if (!read().startsWith("226") && !read().startsWith("226"))
            throw new RuntimeException("Error in NLST, either you did not call PASV previously or the file/dir does not exist");
        try {
            while (transfererIn.available() < 1) Thread.sleep(100);
        } catch (InterruptedException e) {/**/}
        byte[] data = new byte[transfererIn.available()];
        transfererIn.readFully(data);
        debug(new String(data));
        return new String(data).split("\r\n");
    }

    public final String[] list(String file) throws IOException {
        sendRawLine("LIST " + file);
        if (!read().startsWith("150"))
            throw new RuntimeException("Error in LIST, either you did not call PASV previously or the file/dir does not exist");
        try {
            while (transfererIn.available() < 1) Thread.sleep(300);
        } catch (InterruptedException e) {/**/}
        byte[] data = new byte[transfererIn.available()];
        transfererIn.readFully(data);
        debug(new String(data));
        return new String(data).split("\r\n");
    }

    public final int size(String fileName) throws IOException {
        sendRawLine("SIZE " + fileName);
        String line;
        if (!(line = read()).startsWith("213"))
            throw new RuntimeException("Invalid filename!");
        return Integer.parseInt(line.substring(4).replace("\r\n", ""));
    }

    public final long lastModified(String fileName) throws IOException {
        sendRawLine("MDTM " + fileName);
        String line;
        if (!(line = read()).startsWith("213"))
            throw new IOException("Error with MDTM");
        return Long.parseLong(line.substring(4).replace("\r\n", ""));
    }

    public final boolean mkdir(String dir) throws IOException {
        sendRawLine("MKD " + dir);
        if (read().startsWith("257"))
            return true;
        return false;
    }

    public final boolean rmdir(String dir) throws IOException {
        sendRawLine("RMD " + dir);
        if (read().startsWith("250"))
            return true;
        return false;
    }

    public final void rename(String old, String nEw) throws IOException {
        sendRawLine("RNFR " + old);
        if (!read().startsWith("350"))
            throw new IOException("Cannot rename file, it doesn't exist!");
        sendRawLine("RNTO " + nEw);
        if (!read().startsWith("250"))
            throw new IOException("Error renaming file!");
    }

    public final void setType(String type) throws IOException {
        sendRawLine("TYPE " + type);
        if (!read().startsWith("200"))
            throw new RuntimeException("Invalid type!");
    }

    public final void ascii() throws IOException {
        setType("A");
    }

    public final void bin() throws IOException {
        setType("I");
    }

    private String read() throws IOException {
        try {
            while (in.available() < 1) Thread.sleep(100);
        } catch (InterruptedException e) {/**/}
        byte[] data = new byte[in.available()];
        in.readFully(data);
        debug(new String(data));
        return new String(data);
    }

    public final void sendRawLine(String line) throws IOException {
        out.write((line + "\r\n").getBytes());
        debug(line);
    }

    public final void debug(String line) {
        if (DEBUG)
            System.out.println(line);
    }

    public final boolean isDebug() {
        return DEBUG;
    }

    public final void setDebug(boolean d) {
        DEBUG = d;
    }
}