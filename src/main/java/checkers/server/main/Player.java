package checkers.server.main;

import java.io.*;
import java.net.Socket;

public class Player implements Runnable {
    private Socket socket;
    private String color;
    private PrintWriter out;
    private BufferedReader in;
    private boolean finished = false;

    public Player(Socket socket, String color) {
        this.socket = socket;
        this.color = color;
        System.out.println(color);
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        out.println("color="+color);
        while (!finished) {
            process();
        }
    }

    private void process() {
        try {
            String str = in.readLine();
            if (str.isEmpty()) finished = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
