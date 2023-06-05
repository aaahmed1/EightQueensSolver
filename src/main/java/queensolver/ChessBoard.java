package queensolver;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessBoard {
    private int[][] board = new int[8][8];
    private int[] originalPositions = new int[8];
    private int[] positions = new int[8]; //which row in ith column has queen
    private int restarts = 0;
    private int stateChanges = 0;
    private List<TranslateTransition> moves = new ArrayList<>();
    private SequentialTransition animate;
    private GridPane grid;
    private boolean done = false;
    private double cursorX;
    private double cursorY;

    public ChessBoard(GridPane grid) {
        this.grid = grid;
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));
        grid.getColumnConstraints().add(new ColumnConstraints(62.5));

        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
        grid.getRowConstraints().add(new RowConstraints(62.5));
    }

    public void placeInitialQueens() throws IOException {
        for (int[] a : board) {
            Arrays.fill(a, 0);
        }
        for (int k = 0; k < board[0].length; k++) {
            int index = (int)(Math.random() * 8);
            positions[k] = index;
            originalPositions[k] = index;
            board[index][k] = 1;
            ImageView queen = createQueen();
            queen.setFitHeight(62.5);
            queen.setFitWidth(62.5);
            StackPane stack = getFromGrid(index, k);
            stack.getChildren().add(queen);
        }
    }

    public void placeQueens() {
        for (int[] a : board) {
            Arrays.fill(a, 0);
        }
        for (int k = 0; k < board[0].length; k++) {
            int index = (int)(Math.random() * 8);
            positions[k] = index;
            board[index][k] = 1;
        }
    }

    public void moveQueens() throws IOException {
        disableQueens(); //prevents queens from being draggable during animation
        int restarts = 0;
        int stateChanges = 0;
        for (;;) {
            int currentH = countConflicts();
            if (currentH == 0) break;
            int hCount = 0;
            boolean madeMove = false;
            //printBoard();

            for (int k = 0; k < board[0].length; k++) {
                for (int i = 0; i < board.length; i++) {
                    if (board[i][k] == 1) continue;
                    int conflicts = tryMove(k, i);
                    if (conflicts < currentH) {
                        hCount++;
                        if (!madeMove) {
                            makeMove(k, i);
                            madeMove = true;
                        }
                    }
                }
            }
            //System.out.println("Neighbors found with lower h: " + hCount);
            stateChanges++;
            if (!madeMove) {
                restart();
                restarts++;
            }
        }
        TranslateTransition[] transitions = new TranslateTransition[moves.size()];
        moves.toArray(transitions);
        animate = new SequentialTransition(transitions);
        animate.play();
        System.out.println("State changes: " + stateChanges);
        System.out.println("Restarts: " + restarts);
    }

    public int countConflicts() {
        int count = 0;
        for (int i = 0; i < positions.length; i++) {
            //right
            for (int k = i + 1; k < board[0].length; k++) {
                if (board[positions[i]][k] == 1) count++;
            }
            //left
            for (int k = i - 1; k >= 0; k--) {
                if (board[positions[i]][k] == 1) count++;
            }
            //top right diagonal
            int row = positions[i] - 1;
            int col = i + 1;
            while (row >= 0 && col < board[0].length) {
                if (board[row][col] == 1) count++;
                row--;
                col++;
            }
            //top left diagonal
            row = positions[i] - 1;
            col = i - 1;
            while (row >= 0 && col >= 0) {
                if (board[row][col] == 1) count++;
                row--;
                col--;
            }
            //bottom right diagonal
            row = positions[i] + 1;
            col = i + 1;
            while (row < board.length && col < board[0].length) {
                if (board[row][col] == 1) count++;
                row++;
                col++;
            }
            //bottom left diagonal
            row = positions[i] + 1;
            col = i - 1;
            while (row < board.length && col >= 0) {
                if (board[row][col] == 1) count++;
                row++;
                col--;
            }
        }
        return count / 2;

    }

    public int tryMove(int col, int newRow) {
        int original = positions[col];
        board[positions[col]][col] = 0;
        positions[col] = newRow;
        board[newRow][col] = 1;
        int res = countConflicts();
        positions[col] = original;
        board[newRow][col] = 0;
        board[original][col] = 1;
        return res;
    }

    public void makeMove(int col, int newRow) {
        grid.layout();
        int oldRow = originalPositions[col];
        ImageView queen = (ImageView)getFromGrid(oldRow, col).getChildren().get(0);
        TranslateTransition tt = new TranslateTransition();
        tt.setDuration(Duration.seconds(1));
        int rows = Math.abs(oldRow - newRow);
        if (newRow < oldRow) tt.setToY(-1 * (62.5 * rows));
        else tt.setToY(62.5 * rows);
        tt.setNode(queen);
        tt.setCycleCount(1);
        moves.add(tt);
        board[positions[col]][col] = 0;
        positions[col] = newRow;
        board[newRow][col] = 1;
    }

    public void printBoard() {
        System.out.println("********");
        for (int i = 0; i < board.length; i++) {
            for (int k = 0; k < board[0].length; k++) {
                if (k == board[0].length - 1) System.out.println(board[i][k]);
                else System.out.print(board[i][k] + ",");
            }
        }
    }

    public void restart() throws IOException {
        grid.layout();
        placeQueens();
        for (int i = 0; i < 8; i++) {
            int oldRow = originalPositions[i];
            ImageView queen = (ImageView)getFromGrid(oldRow, i).getChildren().get(0);
            TranslateTransition tt = new TranslateTransition();
            tt.setDuration(Duration.millis(500));
            int newRow = positions[i];
            int rows = Math.abs(oldRow - newRow);
            if (newRow < oldRow) tt.setToY(-1 * (62.5 * rows));
            else tt.setToY(62.5 * rows);
            tt.setNode(queen);
            tt.setCycleCount(1);
            moves.add(tt);
        }
        //TranslateTransition[] transitions = new TranslateTransition[moves.size()];
        //moves.toArray(transitions);
        //animate = new SequentialTransition(transitions);
        //animate.setOnFinished((e) -> done = true);
        //animate.play();
        //while (!done);
        //grid.getChildren().clear();
        //moves.clear();
    }

    public StackPane getFromGrid(int row, int col) {
        for (Node stack : grid.getChildren()) {
            if (GridPane.getColumnIndex(stack) == col && GridPane.getRowIndex(stack) == row) return (StackPane) stack;
        }
        return null;
    }

    public void setupGrid() throws IOException {
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                StackPane stack = new StackPane();
                stack.setPrefHeight(62.5);
                stack.setPrefWidth(62.5);
                stack.setOnMouseDragReleased((e) -> {
                    ImageView queen = (ImageView)e.getGestureSource();
                    //System.out.println("X: " + GridPane.getColumnIndex(stack));
                    //System.out.println("Y: " + GridPane.getRowIndex(stack));
                    StackPane parent = (StackPane)queen.getParent();
                    int newRow = GridPane.getRowIndex(stack);
                    int newCol = GridPane.getColumnIndex(stack);
                    int oldRow = GridPane.getRowIndex(parent);
                    int oldCol = GridPane.getColumnIndex(parent);
                    if (newCol != oldCol) {
                        queen.setTranslateY(0);
                        queen.setTranslateX(0);
                        e.consume();
                        return;
                    }
                    board[oldRow][oldCol] = 0;
                    board[newRow][newCol] = 1;
                    positions[newCol] = newRow;
                    originalPositions[newCol] = newRow;
                    queen.setTranslateY(0);
                    queen.setTranslateX(0);
                    parent.getChildren().remove(queen);
                    try {
                        ImageView newQueen = createQueen();
                        stack.getChildren().add(newQueen);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.consume();
                });
                stack.setOnMouseDragEntered((e) -> {
                    StackPane parent = (StackPane)((ImageView)e.getGestureSource()).getParent();
                    int oldCol = GridPane.getColumnIndex(parent);
                    int newCol = GridPane.getColumnIndex(stack);
                    if (oldCol != newCol) {
                        stack.setStyle("-fx-border-color: red; -fx-border-width: 2px");
                    }
                    else {
                        stack.setStyle("-fx-border-color: green; -fx-border-width: 2px");
                    }
                });
                stack.setOnMouseDragExited((e) -> {
                    stack.setStyle("");
                });
                grid.add(stack, k, i);
            }
        }
    }

    public ImageView createQueen() throws IOException {
        File file2 = new File("src/main/resources/images/blackqueen.png");
        Image image2 = new Image(file2.toURI().toString());
        ImageView queenTest = new ImageView(image2);
        queenTest.setFitHeight(62.5);
        queenTest.setFitWidth(62.5);
        setupQueen(queenTest);
        return queenTest;
    }

    public void setupQueen(ImageView queen) {
        queen.setOnMouseEntered((e) -> {
            queen.setCursor(Cursor.OPEN_HAND);
        });
        queen.setOnMousePressed((e) -> {
            //System.out.println("Scene y: " + e.getSceneY());
            //System.out.println("Local bounds Y: " + queen.localToScene(queen.getBoundsInLocal()).getMinY());
            //System.out.println("Local bounds X: " + queen.localToScene(queen.getBoundsInLocal()).getMinX());
            queen.setCursor(Cursor.CLOSED_HAND);
            queen.setMouseTransparent(true);
            //cursorX = e.getSceneX();
            //cursorY = e.getSceneY();
            //System.out.println("Cursor x: " + cursorX);
            //System.out.println("Cursor y: " + cursorY);
        });
        queen.setOnMouseDragged((e) -> {
            //System.out.println(e.getEventType().getName());
            //root.layout();
            //System.out.println("LayoutY: " + queen.getLayoutY());
            queen.setCursor(Cursor.CLOSED_HAND);
            //queen.setTranslateX(e.getSceneX() - cursorX);
            //queen.setTranslateY(e.getSceneY() - cursorY);
            //queen.relocate(e.getX(), e.getY());
            //System.out.println("y: " + queen.getTranslateY());
            //System.out.println("x: " + queen.getTranslateX());
        });
        queen.setOnDragDetected((e) -> {
            //System.out.println(e.getEventType().getName());
            queen.startFullDrag();
        });
        queen.setOnMouseReleased((e) -> {
            queen.setCursor(Cursor.OPEN_HAND);
            queen.setMouseTransparent(false);
            //queen.setTranslateY(0);
            //queen.setTranslateX(0);
            //vbox.getChildren().remove(queen);
        });
    }

    public void disableQueens() {
        for (int i = 0; i < originalPositions.length; i++) {
            StackPane stack = getFromGrid(originalPositions[i], i);
            stack.getChildren().get(0).setOnDragDetected(null);
        }
    }
}
