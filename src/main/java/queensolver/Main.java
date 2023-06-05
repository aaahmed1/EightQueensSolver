package queensolver;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {
    BorderPane root;
    GridPane grid;
    HBox hbox;
    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new BorderPane();
        grid = new GridPane();
        hbox = new HBox();
        hbox.setStyle("-fx-background-color: tan;");
        root.setTop(hbox);
        root.setCenter(grid);
        hbox.setPrefWidth(500);
        hbox.setPrefHeight(50);
        Button start = new Button("Start");
        hbox.getChildren().add(start);
        hbox.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Chess Solver");
        File file = new File("src/main/resources/images/chessboard.jpeg");
        Image image = new Image(file.toURI().toString());
        BackgroundImage background = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(500, 500, false, false, true, false));
        grid.setBackground(new Background(background));
        grid.setPrefWidth(500);
        grid.setPrefHeight(500);
        primaryStage.setResizable(false);
        ChessBoard board = new ChessBoard(grid);
        board.setupGrid();
        board.placeInitialQueens();
        AtomicBoolean done = new AtomicBoolean(false);
        start.setOnAction((e) -> {
            if (!done.get()) {
                try {
                    board.moveQueens();
                    done.set(true);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        root.setOnMouseDragReleased((e) -> {
            ImageView queen = (ImageView) e.getGestureSource();
            queen.setTranslateY(0);
            queen.setTranslateX(0);
        });
        //setupQueen(queenTest);
        // grid.add(queenTest, 0, 0);
        primaryStage.setScene(new Scene(root, 500, 550));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}


