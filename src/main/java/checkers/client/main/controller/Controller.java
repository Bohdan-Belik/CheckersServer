package checkers.client.main.controller;

import checkers.client.main.Piece;
import checkers.client.main.model.State;
import checkers.client.main.util.Pair;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static checkers.client.main.GameConstants.IMPOSSIBLE;

public class Controller {
    public final static int SIZE = 40;
    int W = 25;
    int H = 17;
    private int[][] field;
    private Color myColor;
    private List<Piece> pieces = new ArrayList<>();
    private State state;


    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @FXML private Canvas canvas;
    @FXML private Pane pane;

    public void initialize() {

        connectToServer();

        field = createField();
        int col = field[0][0];
        myColor = Color.rgb(col / 0x100 / 0x100, col / 0x100 % 0x100, col % 0x100); // ff 00 ff 0x100

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().addListener(((observable, oldValue, newValue) -> draw()));
        canvas.heightProperty().addListener(((observable, oldValue, newValue) -> draw()));
    }

    private void printField(int[][] field) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                System.out.print(field[i][j]);
            }
            System.out.println();
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[][] createField() {
        try {
            String s = in.readLine();
            field = new int[H][W];
            int k = 0;
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    field[i][j] = s.charAt(k++) - '0';
                    if (field[i][j] > 0 && field[i][j] < 9) {
                        pieces.add(new Piece(i, j, field[i][j]));
                    }
                }
            }
            // Color 0xff0000ff
            field[0][0] = Integer.parseInt(s.substring(H*W+2, H*W+8),16);
            return field;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (Piece piece : pieces) {
            double y = piece.getRow() * SIZE * Math.sqrt(3) / 2.0;
            double x = piece.getColumn() * SIZE / 2.0;
            gc.setFill(piece.getColor());
            gc.fillOval(x, y, SIZE, SIZE);
            if (piece.isSelected()) {
                // draw selection
                gc.strokeOval(x + SIZE / 4.0, y + SIZE / 4.0, SIZE / 2.0, SIZE / 2.0);
            }
        }
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                if ((i+j)%2 == 0 && field[i][j] < IMPOSSIBLE) {
                    double y = i * SIZE * Math.sqrt(3) / 2.0;
                    double x = j * SIZE / 2.0;
                    gc.strokeOval(x, y, SIZE, SIZE);
                }
            }
        }
        gc.setFill(myColor);
        gc.fillRect(0, H * SIZE * Math.sqrt(3) / 2 + SIZE, canvas.getWidth(), SIZE);
    }

    public void processMove(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        Pair p = findCell(x,y);
        // todo checkCell
        if (p != null) {
            Optional<Piece> f = findPiece(p);
            if (f.isEmpty()) {
                System.out.println("none");
            } else {
                if (f.get().getColor().equals(myColor)) {
                    System.out.println("ok");
                    Piece piece = f.get();
                    select(piece);
                    draw();
                } else {
                    System.out.println("err");
                }
            }
        }
    }

    private void select(Piece piece) {
        for (Piece p : pieces) {
            p.setSelected(p.equals(piece));
        }
    }

    private Optional<Piece> findPiece(Pair p) {
        return pieces.stream().filter(f -> f.getRow() == p.getI() && f.getColumn() == p.getJ()).findFirst();
    }

    private Pair findCell(double x, double y) {
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                if ((i+j)%2 == 0 && field[i][j] < IMPOSSIBLE) {
                    double yc = i * SIZE * Math.sqrt(3) / 2.0 + SIZE / 2.0;
                    double xc = j * SIZE / 2.0 + SIZE / 2.0;
                    if (Math.hypot(x-xc, y-yc) < SIZE / 2.0) return new Pair(i,j);
                }
            }
        }
        return null;
    }
}
