package fi.tuni.prog3.wordle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


/**
 * JavaFX App
 */
public class Wordle extends Application {

    private static GridPane grid;

    private static TextField[][] cells;
    private static ArrayList<String> words;
    private static String currentWord;
    private static int currentWordIdx;
    private static final int maxGuesses = 6;

    private static int guessCount = 0;
    private static String currentGuess = "";

    private static Text info;

    private static boolean gameOn = true;

    private static final int radius = 2;
    private static final Background bgWhite = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(radius), Insets.EMPTY));
    private static final Background bgOrange = new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(radius), Insets.EMPTY));
    private static final Background bgGreen = new Background(new BackgroundFill(Color.GREEN, new CornerRadii(radius), Insets.EMPTY));
    private static final Background bgBeige = new Background(new BackgroundFill(Color.BURLYWOOD, new CornerRadii(radius), Insets.EMPTY));
    private static final Background bgGrey = new Background(new BackgroundFill(Color.GREY, new CornerRadii(radius), Insets.EMPTY));

    @Override
    public void start(Stage stage) {
        VBox vb = new VBox();

        initGrid();

        Scene scene = new Scene(vb);

        HBox top = buildTopRow();
        vb.getChildren().addAll(top, grid);
        vb.setFocusTraversable(false);

        words = loadWords();
        currentWordIdx = 0;
        currentWord = loadWords().get(currentWordIdx);
        initGame();
        scene.setOnKeyPressed(this::handleKeyInputs);

        stage.setScene(scene);
        stage.setTitle("Worlde");
        grid.requestFocus();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


    private static void initGame() {
        reset();
        buildGrid();
        grid.requestFocus();
    }

    private static void initNewGame(ActionEvent event) {
        currentWordIdx += 1;
        currentWord = words.get(currentWordIdx);
        initGame();
    }

    private static void reset() {
        gameOn = true;
        guessCount = 0;
        currentGuess = "";
    }

    private static void initNextGuess() {
        guessCount += 1;
        currentGuess = "";
    }

    private static HBox buildTopRow() {
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER);
        Button btn = new Button("Start new game");
        btn.setId("newGameBtn");
        btn.setFocusTraversable(false);
        btn.setOnAction(Wordle::initNewGame);
        info = new Text();
        info.setId("infoBox");

        top.getChildren().addAll(btn, info);
        return top;
    }


    private static void initGrid() {
        grid = new GridPane();
        // grid.setGridLinesVisible(true);
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setBackground(bgBeige);
    }

    private static void buildGrid() {
        grid.getChildren().clear();

        int wordLength = currentWord.length();
        cells = new TextField[maxGuesses][wordLength];

        grid.getColumnConstraints().clear();

        for (int i = 0; i < maxGuesses; i++) {
            for (int j = 0; j < wordLength; j++) {
                TextField newCell = buildCharField();
                grid.add(newCell, j, i);
                GridPane.setMargin(newCell, new Insets(2, 0, 2, 0));
                newCell.setId(String.format("%d_%d", i, j));
                cells[i][j] = newCell;
            }
        }
    }

    static private TextField buildCharField() {
        TextField t = new TextField("");
        t.setBackground(bgWhite);
        t.setMaxWidth(60);
        t.setMaxHeight(60);
        t.setMinWidth(60);
        t.setMinHeight(60);
        t.setEditable(false);
        t.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
        t.setFocusTraversable(false);
        return t;
    }

    private void handleKeyInputs(KeyEvent key) {
        if (!gameOn) {
            return;
        }

        if (key.getCode().equals(KeyCode.ENTER)) {
            handleEnter();
            return;
        }
        if (key.getCode().isLetterKey()) {
            addLetter(key.getCode().getChar());
            return;
        }

        if (key.getCode().equals(KeyCode.BACK_SPACE)) {
            deleteLetter();
        }
    }

    private void handleEnter() {
        if (currentGuess.length() == currentWord.length()) {
            boolean wonGame = checkGameState();
            if (!wonGame && guessCount + 1 < maxGuesses) {
                initNextGuess();
            } else if (!wonGame) {
                info.setText("Game over, you lost!");
            }
        } else {
            info.setText("Give a complete word before pressing Enter!");
        }
    }

    private static boolean checkGameState() {
        if (currentGuess.equals(currentWord)) {
            info.setText("Congratulations, you won!");
            for (TextField t : cells[guessCount]) {
                t.setBackground(bgGreen);
            }
            gameOn = false;
            return true;
        }
        checkCorrectLetters();
        return false;
    }


    private static void checkCorrectLetters() {

        for (TextField t : cells[guessCount]) {
            t.setBackground(bgGrey);
        }

        for (int i = 0; i < currentWord.length(); i++) {
            if (currentGuess.contains((String.valueOf(currentWord.charAt(i))))) {
                int idx = 0;
                int prev_idx = 0;
                while ((idx = currentGuess.indexOf(currentWord.charAt(i), prev_idx)) != -1) {
                    cells[guessCount][idx].setBackground(bgOrange);
                    prev_idx = idx + 1;
                    if (prev_idx >= currentWord.length()) {
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < currentGuess.length(); i++) {
            if (currentWord.charAt(i) == currentGuess.charAt(i)) {
                cells[guessCount][i].setBackground(bgGreen);
            }
        }
    }

    private static void addLetter(String c) {
        if (currentGuess.length() + 1 > currentWord.length()) {
            return;
        }
        currentGuess += c;

        updateLettersUI();

    }

    private static void deleteLetter() {
        if (currentGuess.isEmpty()) {
            return;
        }
        currentGuess = currentGuess.substring(0, currentGuess.length() - 1);
        updateLettersUI();
    }

    private static void updateLettersUI() {
        int i;
        for (i = 0; i < currentGuess.length(); i++) {
            cells[guessCount][i].setText(String.valueOf(currentGuess.charAt(i)));
        }

        for (; i < currentWord.length(); i++) {
            cells[guessCount][i].setText("");
        }


    }


    private ArrayList<String> loadWords() {
        ArrayList<String> words = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader("words.txt"))) {
            String line = "";
            while ((line = r.readLine()) != null) {
                words.add(line.toUpperCase());
            }
        } catch (Exception e) {
            System.out.printf("Reading words file failed :\n %s", e.getMessage());
        }
        return words;
    }
}